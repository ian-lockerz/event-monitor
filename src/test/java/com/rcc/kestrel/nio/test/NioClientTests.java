package com.lockerz.kestrel.nio.test;

import com.lockerz.kestrel.AsynchronousClient;
import com.lockerz.kestrel.nio.Client;
import com.lockerz.kestrel.async.*;

import com.lockerz.event.Login;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;

import org.junit.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import static java.util.concurrent.TimeUnit.*;

public class NioClientTests {
    private Client client;
    private String queueName;

    @Before
    public void setup() throws IOException {
        String hostname = System.getProperty("kestrel.hostname");
        int port = Integer.getInteger("kestrel.port").intValue();
        this.client = new Client(hostname, port, 200000, 512);
        client.init();

        this.queueName = "test_" + randomAlphabetic(32);

        // Ensure that the queue is empty
        //while (client.get(queueName, None).isDefined) {
            //println("Removed item off queue: " + queueName)
        //}
    }

    @After
    public void tearDown() {
        this.client.disconnect();
    }

/*
    @Test
    public void testSet() throws Throwable {
        final CountDownLatch writeLatch = new CountDownLatch(1);
        final byte[] value = "ian".getBytes();

        final boolean[] passed = new boolean[] { false };

        this.client.set(this.queueName, 0L, value, new SetResponseHandler() {
            public void onSuccess() {
                try {
                    synchronized (passed) { passed[0] = true; }
                } finally {
                    writeLatch.countDown();
                }
            }

            public void onError(String type, String message) {
                System.out.println("Error on set :(");
            }
        });

        writeLatch.await(2, SECONDS);
        assertEquals(0, writeLatch.getCount());
        synchronized (passed) { assertTrue(passed[0]); }

        //System.out.println("Sleeping for 60 seconds");
        //Thread.sleep(60000);
        //System.out.println("Done sleeping");

        synchronized (passed) { passed[0] = false; }
        final CountDownLatch readLatch = new CountDownLatch(1);
        this.client.get(this.queueName, 0L, false, new GetResponseHandler() {
            public void onSuccess(byte[] data) {
                try {
                    synchronized (passed) { passed[0] = true; }
                } finally {
                    readLatch.countDown();
                }
            }

            public void onError(String type, String message) {
                System.out.println("Error on set :(");
            }
        });

        readLatch.await(2, SECONDS);
        assertEquals(0, readLatch.getCount());
        synchronized (passed) { assertTrue(passed[0]); }
    }

    @Test
    public void testBinarySet() throws Throwable {
        final Login login = new Login(1, System.currentTimeMillis());
        TSerializer serializer = new TSerializer(new TBinaryProtocol.Factory());
        final TDeserializer deserializer = new TDeserializer(new TBinaryProtocol.Factory());
        final byte[] value = serializer.serialize(login);

        final CountDownLatch writeLatch = new CountDownLatch(1);

        final boolean[] passed = new boolean[] { false };

        this.client.set(this.queueName, 0L, value, new SetResponseHandler() {
            public void onSuccess() {
                try {
                    synchronized (passed) { passed[0] = true; }
                } finally {
                    writeLatch.countDown();
                }
            }

            public void onError(String type, String message) {
                System.out.println("Error on set :(");
            }
        });

        writeLatch.await();

        synchronized (passed) { assertTrue(passed[0]); }

        synchronized (passed) { passed[0] = false; }
        final CountDownLatch readLatch = new CountDownLatch(1);
        this.client.get(this.queueName, 0L, false, new GetResponseHandler() {
            public void onSuccess(byte[] data) {
                try {
                    synchronized (passed) {
                        Login login2 = new Login();
                        deserializer.deserialize(login2, data);
                        if (login.equals(login2)) {
                            passed[0] = true;
                        }
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                } finally {
                    readLatch.countDown();
                }
            }

            public void onError(String type, String message) { ; }
        });

        readLatch.await();

        synchronized (passed) { assertTrue(passed[0]); }
    }

    @Test
    public void testExpiration() throws Throwable {
        final byte[] value = "ian".getBytes();

        final boolean[] passed = new boolean[] { false };

        final CountDownLatch writeLatch = new CountDownLatch(1);
        client.set(this.queueName, 1L, value, new SetResponseHandler() {
            public void onSuccess() {
                try {
                    synchronized (passed) { passed[0] = true; }
                } finally {
                    writeLatch.countDown();
                }
            }

            public void onError(String type, String message) {
                System.out.println("Error on set :(");
            }
        });

        writeLatch.await(2, SECONDS);

        synchronized (passed) { assertTrue(passed[0]); }

        Thread.sleep(2000);

        synchronized (passed) { passed[0] = false; }
        final CountDownLatch readLatch = new CountDownLatch(1);
        this.client.get(this.queueName, 0L, false, new GetResponseHandler() {
            public void onSuccess(byte[] data) {
                try {
                    if (data.length == 0) {
                        synchronized (passed) { passed[0] = true; }
                    }
                } finally {
                    readLatch.countDown();
                }
            }

            public void onError(String type, String message) { ; }
        });

        readLatch.await(2, SECONDS);
        synchronized (passed) { assertTrue(passed[0]); }
    }

    @Test
    public void testSetAndForget() throws Throwable {
        final byte[] value = "ian".getBytes();

        client.setAndForget(this.queueName, 1L, value);

        final boolean[] passed = new boolean[] { false };

        final CountDownLatch readLatch = new CountDownLatch(1);
        this.client.get(this.queueName, 10000L, false, new GetResponseHandler() {
            public void onSuccess(byte[] data) {
                try {
                    synchronized (passed) {
                        passed[0] = true;
                    }
                } finally {
                    readLatch.countDown();
                }
            }

            public void onError(String type, String message) {
                System.out.println("Error on set :(");
            }
        });

        readLatch.await(2, SECONDS);

        synchronized (passed) {
            assertTrue(passed[0]);
        }
    }

    @Test
    public void testPeek() throws Throwable {
        final CountDownLatch writeLatch = new CountDownLatch(1);
        final byte[] value = "ian".getBytes();

        final boolean[] passed = new boolean[] { false };

        this.client.set(this.queueName, 0L, value, new SetResponseHandler() {
            public void onSuccess() {
                try {
                    synchronized (passed) { passed[0] = true; }
                } finally {
                    writeLatch.countDown();
                }
            }

            public void onError(String type, String message) { ; }
        });

        writeLatch.await(2, SECONDS);

        synchronized (passed) { assertTrue(passed[0]); }

        synchronized (passed) { passed[0] = false; }

        final CountDownLatch readLatch = new CountDownLatch(1);
        this.client.peek(this.queueName, 0L, new GetResponseHandler() {
            public void onSuccess(byte[] data) {
                try {
                    synchronized (passed) { passed[0] = true; }
                } finally {
                    readLatch.countDown();
                }
            }

            public void onError(String type, String message) { ; }
        });

        readLatch.await(2, SECONDS);

        synchronized (passed) { assertTrue(passed[0]); }
    }

    @Test
    public void testReliableGet() throws Throwable {
        final CountDownLatch writeLatch = new CountDownLatch(1);
        final byte[] value = "ian".getBytes();

        final boolean[] passed = new boolean[] { false };

        this.client.set(this.queueName, 0L, value, new SetResponseHandler() {
            public void onSuccess() {
                try {
                    synchronized (passed) { passed[0] = true; }
                } finally {
                    writeLatch.countDown();
                }
            }

            public void onError(String type, String message) {
                System.out.println("Error on set :(");
            }
        });

        writeLatch.await(2, SECONDS);

        synchronized (passed) { assertTrue(passed[0]); }

        synchronized (passed) { passed[0] = false; }

        final CountDownLatch readLatch = new CountDownLatch(1);
        this.client.get(this.queueName, 0L, true, new GetResponseHandler() {
            public void onSuccess(byte[] data) {
                try {
                    synchronized (passed) { passed[0] = true; }
                } finally {
                    readLatch.countDown();
                }
            }

            public void onError(String type, String message) {
                System.out.println("Error on set :(");
            }
        });

        readLatch.await(2, SECONDS);

        synchronized (passed) { assertTrue(passed[0]); }
    }
*/

    @Test
    public void testSetAndForgetRandomLoadWithThreadPool() throws Throwable {
        int iters = 10000;

        final Set<String> values = new HashSet<String>();
        for (int i = 0; i < iters; i++) {
            while (!values.add(randomAlphabetic(128)));
        }

        assertEquals(iters, values.size());

        for (String v : values) {
            client.setAndForget(this.queueName, 120L, v.getBytes());
        }

        /*
        final CountDownLatch writeLatch = new CountDownLatch(iters);
        final int[] errorCount = new int[] { 0 };

        for (String v : values) {
            this.client.set(this.queueName, 120L, v.getBytes(), new SetResponseHandler() {
                public void onSuccess() {
                    writeLatch.countDown();
                }

                public void onError(String type, String message) {
                    System.out.println(String.format("ERROR [%s] [%s]", type, message));
                    synchronized (errorCount) { errorCount[0] = errorCount[0] + 1; }
                }
            });
        }

        synchronized (errorCount) { assertEquals(0, errorCount[0]); }
        writeLatch.await(10, SECONDS);
        assertEquals(0, writeLatch.getCount());
        */

        final CountDownLatch readLatch = new CountDownLatch(iters);
        for (int i = 0; i < iters; i++) {
            client.get(this.queueName, 2000L, false, new GetResponseHandler() {
                public void onSuccess(byte[] data) {
                    try {
                        synchronized (values) {
                            values.remove(new String(data));
                        }
                    } finally {
                        readLatch.countDown();
                    }
                }

                public void onError(String type, String message) {
                    System.out.println(String.format("ERROR [%s] [%s]", type, message));
                    readLatch.countDown();
                }
            });
        }

        readLatch.await(10, SECONDS);
        assertEquals(0, readLatch.getCount());
        synchronized (values) { assertEquals(0, values.size()); }
    }

/*
    @Test
    public void testSetAndForgetRandomLoad() throws Throwable {
        int iters = 100000;

        final Set<String> values = new HashSet<String>();
        for (int i = 0; i < iters; i++) {
            while (!values.add(randomAlphabetic(128)));
        }

        assertEquals(iters, values.size());

        for (String v : values) {
            client.setAndForget(this.queueName, 120L, v.getBytes());
        }

        final CountDownLatch latch = new CountDownLatch(iters);
        for (int i = 0; i < iters; i++) {
            client.get(this.queueName, 2000L, false, new GetResponseHandler() {
                public void onSuccess(byte[] data) {
                    try {
                        synchronized (values) {
                            values.remove(new String(data));
                        }
                    } finally {
                        latch.countDown();
                    }
                }

                public void onError(String type, String message) {
                    System.out.println(String.format("ERROR [%s] [%s]", type, message));
                    latch.countDown();
                }
            });
        }

        latch.await(60, SECONDS);

        assertEquals(0, latch.getCount());

        synchronized (values) { assertEquals(0, values.size()); }
    }
*/

/*
    @Test
    public void testSetAndForgetLoad() throws Throwable {
        final byte[] value = "ian".getBytes();

        final boolean[] passed = new boolean[] { true };

        for (int i = 0; i < 10000; i++) {
            client.setAndForget(this.queueName, 120L, value);
        }

        final CountDownLatch latch = new CountDownLatch(10000);
        for (int i = 0; i < 10000; i++) {
            client.get(this.queueName, 2000L, false, new GetResponseHandler() {
                public void onSuccess(byte[] data) {
                    try {
                        if (!Arrays.equals(data, value)) {
                            synchronized (passed) { passed[0] = false; }
                        }
                    } finally {
                        latch.countDown();
                    }
                }

                public void onError(String type, String message) {
                    System.out.println(String.format("ERROR [%s] [%s]", type, message));
                    synchronized (passed) { passed[0] = false; }
                    latch.countDown();
                }
            });
        }

        latch.await();

        synchronized (passed) { assertTrue(passed[0]); }
    }
*/

/*
    @Test
    public void testLoginSetAndForgetLoad() throws Throwable {
        Login login = new Login(1, System.currentTimeMillis());
        TSerializer serializer = new TSerializer(new TBinaryProtocol.Factory());
        final byte[] value = serializer.serialize(login);

        for (int i = 0; i < 1000; i++) {
            client.setAndForget(this.queueName, 1L, value);
        }
        Thread.sleep(10000);
    }
*/
}
