package com.udf;
/*
------------------------------------------------------------------------------
  Name     : Udf.base.KAFKA
  Purpose  : Kafka product & consumer
  Author   : Adam
  Revisions:
  Ver        Date        Author           Description
  ---------  ----------  ---------------  ------------------------------------
  1.0        2024/3/5    Adam             Create.
  1.2        2024/3/8    Adam             putsync, putcb
  1.2.1      2024/10/9   Adam             Fix: KAFKA & getStatus, CNF -> CNFYAML

 Usage:
    property: consumer
    method  : get, put, reset
    import  : BASE
    pom     :
        <!--Kafka-->
        <dependency>
            <groupId>org.apache.kafka</groupId>
            <artifactId>kafka-clients</artifactId>
            <version>2.0.0</version>
        </dependency>
        <dependency>
            <groupId>org.apache.kafka</groupId>
            <artifactId>kafka_2.11</artifactId>
            <version>0.10.0.1</version>
        </dependency>
        <dependency>
            <groupId>org.apache.kafka</groupId>
            <artifactId>kafka-streams</artifactId>
            <version>1.0.0</version>
        </dependency>

------------------------------------------------------------------------------
*/
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.util.*;

import static com.udf.BASE.*;

public class KAFKA {
    public static final String VERSION = "v1.2.1";
    public static Producer<String, String> producer;
    public static KafkaConsumer<String, String> consumer;
    public static ConsumerRecords<String, String> cr;                // set stream
    public static boolean status = false;                            // connect status
    public static boolean statusCnf = false;                         // Kafka configuration
    public static List<String> topics = new ArrayList<>();      // topic list
    public static Map<Long, List<String>> val = new HashMap<>();  // set result, [offset, value]
    public static String jval = "";                                  // json set result
    public static int cs = 0;                                        // rows count
    public static int PullMS = 1000;                                 // default pull max 1000 millisecond
    public static int MaxRows = 500;
    private static final Properties props = new Properties();

    public KAFKA(String mqname1, String mq1) {
        // get db jdbc info
        // using CNF
        // CNF cnf1 = new CNF(mq1);
        // String serv1 = cnf1.get(mqname1 + ".host");      // s1:9092,s2:9092,...
        CNFYAML cnf1 = new CNFYAML(mq1);
        String serv1 = (String) map2map(cnf1.get(mqname1)).get("host");      // s1:9092,s2:9092,...

        _init(serv1);    // props init
        getStatus();     // set status
    }
    public KAFKA(String mqname1) {
        this(mqname1, "db.yaml");
    }

    public static boolean getStatus() {
        Set topics1 = null;
        try {
            AdminClient client1 = KafkaAdminClient.create(props);
            client1.listTopics().names().get();
            status = true;
            client1.close();
        } catch (Exception e) {
            status = false;
            logerror(e);
        }

        return status;
    }
    public static List getTopics() {
        if (!status) return new ArrayList<String>();

        // get topic list
        Set topics1 = null;
        try {
            AdminClient client1 = KafkaAdminClient.create(props);
            topics1 = client1.listTopics().names().get();
            client1.close();
            return new ArrayList<String>(topics1);
        } catch (Exception e) {
            logerror(e);
            return new ArrayList<>();
        }
    }
    public static List getos(String topic1, String group1) {
        _consumer(topic1, group1, 1);
        if (!status) return new ArrayList();

        long osb = 0;                                     // beginOffsets
        long ose = 0;                                     // endOffsets
        long osc = 0;                                     // currentOffsets
        List los1 = new ArrayList();
        Map<Integer, Long> beginOffsetMap = new HashMap<Integer, Long>();
        Map<Integer, Long> endOffsetMap = new HashMap<Integer, Long>();
        Map<Integer, Long> commitOffsetMap = new HashMap<Integer, Long>();

        List<TopicPartition> topicPartitions = new ArrayList<TopicPartition>();
        List<PartitionInfo> partitionsFor;

        partitionsFor = consumer.partitionsFor(topic1);

        for (PartitionInfo partitionInfo : partitionsFor) {
            TopicPartition topicPartition = new TopicPartition(partitionInfo.topic(), partitionInfo.partition());
            topicPartitions.add(topicPartition);
        }

        // beginOffsetMap
        Map<TopicPartition, Long> beginOffsets = consumer.beginningOffsets(topicPartitions);
        for (TopicPartition partitionInfo : beginOffsets.keySet()) {
            beginOffsetMap.put(partitionInfo.partition(), beginOffsets.get(partitionInfo));
        }
//        for (Integer partitionId : beginOffsetMap.keySet()) {
//            logger.info(String.format("topic:%s, partition:%s, logSize:%s", sTopic, partitionId, beginOffsetMap.get(partitionId)));
//        }
        // endOffsetMap
        Map<TopicPartition, Long> endOffsets = consumer.endOffsets(topicPartitions);
        for (TopicPartition partitionInfo : endOffsets.keySet()) {
            endOffsetMap.put(partitionInfo.partition(), endOffsets.get(partitionInfo));
        }
//        for (Integer partitionId : endOffsetMap.keySet()) {
//            logger.info(String.format("topic:%s, partition:%s, logSize:%s", sTopic, partitionId, endOffsetMap.get(partitionId)));
//        }

        //commitOffsetMap
        for (TopicPartition topicAndPartition : topicPartitions) {
            OffsetAndMetadata committed = consumer.committed(topicAndPartition);
            if (committed==null)
                commitOffsetMap.put(topicAndPartition.partition(), 0l);
            else
                commitOffsetMap.put(topicAndPartition.partition(), committed.offset());
        }

        //sum lag
        long lagSum = 0;
        osb = 9999999999999999l;
        ose = 0;
        if (endOffsetMap.size() == commitOffsetMap.size()) {
            for (Integer partition : endOffsetMap.keySet()) {
                long beginOffSet = beginOffsetMap.get(partition);
                long endOffSet = endOffsetMap.get(partition);
                long commitOffSet = commitOffsetMap.get(partition);
                long diffOffset = endOffSet - commitOffSet;
                lagSum += diffOffset;
                osb = Math.min(osb, beginOffSet);
                ose = Math.max(ose, endOffSet);
                osc = Math.max(osc, commitOffSet);
//                logger.info("Topic:" + sTopic + ", groupID:" + sGroup + ", partition:" + partition + ", beginOffSet:" + beginOffSet + ", endOffset:" + endOffSet + ", commitOffset:" + commitOffSet + ", diffOffset:" + diffOffset);
            }
//            logger.info("Topic:" + sTopic + ", groupID:" + sGroup + ", LAG:" + lagSum);
        } else {
            logerror("this topic partitions lost.");
        }
        los1.addAll(Arrays.asList(osb, ose, osc));
        _close();
        return los1;
    }
    // !!!! traversed all the records(cr), consumer needs to be closed.
    // .close()
    public static void getStream(String topic1, String group1, int records1) {
        if (records1 < 1)
            return;

        _consumer(topic1, group1, records1);
        if (!status) return;

        cr = consumer.poll(Duration.ofMillis(PullMS));
        cs = cr.count();
        logwarn("Get records: " + cs);

        //_close();
    }
    public static void put(String topic1, List<String> dat1) {
        producer = new KafkaProducer<>(props);
        for (String s : dat1) {
            producer.send(new ProducerRecord<String, String>(topic1, s));
        }
        producer.close();
        logwarn(String.format("Producer %s records successed.", dat1.size()));
    }
    public static void putsync(String topic1, List<String> dat1) {
        producer = new KafkaProducer<>(props);
        for (String s : dat1) {
            try {
                RecordMetadata metadata = producer.send(new ProducerRecord<String, String>(topic1, s)).get();
                //logger.info("offset = "+metadata.offset());
            } catch (Exception e) {
                logerror(e);
                return;
            }
        }
        producer.close();
        logwarn(String.format("Synchronous Producer %s records successed.", dat1.size()));
    }
    public static void putcb(String topic1, ArrayList<String> dat1) {
        producer = new KafkaProducer<>(props);
        for (String s : dat1) {
            producer.send(new ProducerRecord<String, String>(topic1, s), (RecordMetadata metadata, Exception e) -> {
                if (e != null) {
                    logerror(e);
//                } else {
//                    //System.out.println("offset = " + metadata.offset());
                }
            });
        }
        producer.close();
        log(String.format("CallBack Producer %s records successed.", dat1.size()));
    }
    public static void get(String topic1, String group1, int records1) {
        _consumer(topic1, group1, records1);
        if (!status) return;

        long i;
        cr = consumer.poll(Duration.ofMillis(PullMS));
        cs = cr.count();
        log(String.format("Topic:[%s], Group:[%s], get records: %s", topic1, group1, cs));

        i = 0;
        List lrs1;
        for (ConsumerRecord<String, String> record : cr) {
            lrs1 = new ArrayList();
            lrs1.add(record.offset());
            lrs1.add(record.value());
//            lrs1.add(record.key());
//            lrs1.add(record.timestamp());
            val.put(i++, lrs1);
        }
        _close();
        //jval = json.toJson(val);
    }
    public static void get(String topic1, String group1) {
        get(topic1, group1, MaxRows);
    }
    public static void reset(String topic1, String group1, long offset1) {
        _consumer(topic1, group1, 1);
        if (!status) return;

        Set<TopicPartition> assignment1 = new HashSet<>();
        while (assignment1.size() == 0) {
            consumer.poll(Duration.ofMillis(100));
            assignment1 = consumer.assignment();
        }
        //logger.info("Partition Assignment: " + assignment1);
        Map<TopicPartition, Long> beginOffsets = consumer.beginningOffsets(assignment1);
        Map<TopicPartition, Long> endOffsets = consumer.endOffsets(assignment1);

        for (TopicPartition tp : assignment1) {
            if (offset1 < 0) {
                offset1 = Math.max(beginOffsets.get(tp), endOffsets.get(tp) + offset1 + 1);
            } else {
                offset1 = Math.max(offset1, beginOffsets.get(tp));
                offset1 = Math.min(offset1, endOffsets.get(tp));
            }
            log(String.format("Reset Topic-Partition [%s] offset: [%s,%s]=%s", tp, beginOffsets.get(tp), endOffsets.get(tp), offset1));
            consumer.seek(tp, offset1);
        }
        _close();
    }
    protected static void _init(String serv1) {
        if (isnull(serv1)) return;
        props.put("bootstrap.servers", serv1);
        props.put("request.timeout.ms", 5000);
        //props.put("group.id", sGroup);
        // producer
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", StringSerializer.class.getName());
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 10);
        props.put("buffer.memory", 33554432);
        //consumer
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", StringDeserializer.class.getName());
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("auto.offset.reset", "earliest");
        //props.put("max.poll.records", MaxRows);
        statusCnf = true;
    }
    protected static void _consumer(String topic1, String group1, int records1) {
        if (!statusCnf) {
            logerror("Kafka configuration information not found.");
            return;
        }
        // Create consumer
        _close();
        props.put("group.id", group1);
        props.put("max.poll.records", records1);
        consumer = new KafkaConsumer(props);
        // Subscribe to topic
        consumer.subscribe(Arrays.asList(topic1));
        status = getStatus();
        if (!status) {
            logerror("Kafka Server connect Error.");
            return;
        }
    }
    protected  static void _close() {
        try {
            consumer.close();
        } catch (Exception e) {
            //logerror("Consumer closed.");
        }
        status = false;
    }
}
