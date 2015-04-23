package com.pafc.library.messagebus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.SparseArray;

/**
 * <b><h2>usage:</h2></b> <li>1.invoke {@link com.pafc.library.messagebus.MessageBus#getInstance()} to get the message bus
 * instance, this class is the singleton</li><li>2.use {@link #register(Object)} register the object
 * which has methods want to process the message</li><li>3.add the annotation {@link MessageHandle}
 * or {@link MessageAsyncHandle} to your method
 * <p>
 * <b>** the annotation MUST set the {@link MessageHandle#messageId()} and
 * {@link MessageAsyncHandle#messageId()}, and the method MUST contain zero or one argument, if the
 * argument is the primitive data types, MUST use the object type, such as: int will use
 * Integer**</b>
 * </p>
 * </li><li>4.use the {@link #post} method to post the message, you can pass the object and set
 * delay</li> <li>5. if don't need to process the message, invoke {@link #unRegister(Object)}</li>
 * <li>6. finally, invoke {@link #release()} to release this intance</li> </p>
 *
 * @author idiot2ger
 */

public final class MessageBus implements IMessageBus {


    private static final int DEFAULT_THREAD_NUMS = 5;

    // all class cache
    private Set<Class<?>> mClassCache = new HashSet<Class<?>>();

    // current register object cache, if unreigster, will remove this object from set
    private Map<Class<?>, Set<Object>> mObjectCache = new HashMap<Class<?>, Set<Object>>();

    // method cache
    private SparseArray<List<MethodProcessor>> mProcessorCache = new SparseArray<List<MethodProcessor>>();

    private static MessageBus mInstance;

    private Handler mHandler;

    private ExecutorService mExecutorService;

    private final AtomicInteger mCounter = new AtomicInteger(0);

    private volatile boolean isReleased;

    /**
     * get message bus instance
     *
     * @return
     */
    public static MessageBus getInstance() {
        if (mInstance == null) {
            mInstance = new MessageBus();
        }
        return mInstance;
    }

    @SuppressLint("HandlerLeak")
    private MessageBus() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (!isReleased) {
                    processMessage(msg);
                }
            }
        };

        // thread pool
        mExecutorService = Executors.newFixedThreadPool(DEFAULT_THREAD_NUMS, new ThreadFactory() {
            @Override
            public Thread newThread(final Runnable r) {
                return new Thread() {
                    @Override
                    public void run() {
                        setName("async_handle #" + mCounter.incrementAndGet());
                        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                        r.run();
                    }
                };
            }
        });
    }

    @Override
    public synchronized void register(Object object) {
        if (object == null) {
            throw new IllegalArgumentException("register to the message bus, the object can not be NULL");
        }
        findAnnotationAndCache(object);
    }

    @Override
    public synchronized void unRegister(Object object) {
        if (object == null) {
            throw new IllegalArgumentException("unRegister to the message bus, the object can not be NULL");
        }
        // remove from object cache
        final Class<?> cls = object.getClass();
        Set<Object> objSet = mObjectCache.get(cls);
        if (objSet != null) {
            objSet.remove(object);
        }
    }

    private synchronized void processMessage(final Message message) {
        final int messageId = message.what;
        final List<MethodProcessor> processorList = mProcessorCache.get(messageId);
        final Object messagePassObj = message.obj;
        if (processorList != null) {
            // loop
            for (MethodProcessor p : processorList) {
                if (isReleased) {
                    break;
                }
                Set<Object> objectSet = mObjectCache.get(p.cls);
                if (objectSet != null) {
                    for (Object obj : objectSet) {
                        invokeMethod(p, obj, messagePassObj);
                    }
                }
            }
        }
    }


    private void invokeMethod(final MethodProcessor processor, final Object intanceObj, final Object argObj) {
        final Class<?> pCls = processor.parameterCls;
        final Method method = processor.method;
        if (processor.isAsync) {
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    invokeMethod2(pCls, method, intanceObj, argObj);
                }
            };
            mExecutorService.execute(runnable);
        } else {
            invokeMethod2(pCls, method, intanceObj, argObj);
        }
    }

    private void invokeMethod2(Class<?> parameterCls, Method method, Object intanceObj, Object argObj) {
        try {
            if (parameterCls != null) {
                method.invoke(intanceObj, parameterCls.cast(argObj));
            } else {
                method.invoke(intanceObj, (Object[]) null);
            }
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void post(int messageId) {
        post(messageId, 0);
    }

    @Override
    public void post(int messageId, long delay) {
        mHandler.sendEmptyMessageDelayed(messageId, delay);
    }

    @Override
    public void post(int messageId, Object object) {
        post(messageId, object, 0);
    }

    @Override
    public void post(int messageId, Object object, long delay) {
        final Message message = mHandler.obtainMessage(messageId, object);
        message.setTarget(mHandler);
        mHandler.sendMessageDelayed(message, delay);
    }

    @Override
    public void postImmediate(int messageId) {
        postImmediate(messageId, null);
    }

    @Override
    public void postImmediate(int messageId, Object object) {
        final Message message = mHandler.obtainMessage(messageId, object);
        processMessage(message);
    }

    private void findAnnotationAndCache(Object object) {
        final Class<?> cls = object.getClass();
        // search current cache
        if (!mClassCache.contains(cls)) {
            final Method[] methods = cls.getMethods();
            if (methods != null) {
                boolean isMH, isAMH;
                for (Method method : methods) {
                    isMH = method.isAnnotationPresent(MessageHandle.class);
                    isAMH = method.isAnnotationPresent(MessageAsyncHandle.class);
                    if (isMH && isAMH) {
                        // if method have MessageHandle and MessageAsyncHandle sametime will throws exception
                        throw new RuntimeException("class:" + cls.getName() + ", method:" + method.getName()
                            + " cannot has MessageHandle and MessageAsyncHandle annotations at same time");
                    } else {
                        if (isMH || isAMH) {

                            // here check the method parameter argus
                            final Class<?>[] argClsArray = method.getParameterTypes();
                            if (argClsArray != null && argClsArray.length > 1) {
                                throw new RuntimeException("class:" + cls.getName() + ", method:" + method.getName()
                                    + " MUST have less and equal than one parameters");
                            }


                            // create the processor
                            final MethodProcessor processor = new MethodProcessor();
                            processor.cls = cls;
                            processor.parameterCls = (argClsArray == null || argClsArray.length == 0) ? null : argClsArray[0];
                            if (isMH) {
                                processor.messageId = method.getAnnotation(MessageHandle.class).value();
                            } else if (isAMH) {
                                processor.messageId = method.getAnnotation(MessageAsyncHandle.class).value();
                            }
                            processor.method = method;
                            processor.isAsync = isAMH;


                            List<MethodProcessor> processorList = mProcessorCache.get(processor.messageId);
                            if (processorList == null) {
                                processorList = new ArrayList<MethodProcessor>();
                                mProcessorCache.put(processor.messageId, processorList);
                            }

                            if (!processorList.contains(processor)) {
                                processorList.add(processor);
                            }
                        }
                    }
                }
            }


            // add to the class cache
            mClassCache.add(cls);
        }

        // add object to cache
        Set<Object> objectSet = mObjectCache.get(cls);
        if (objectSet == null) {
            objectSet = new HashSet<Object>();
            mObjectCache.put(cls, objectSet);
        }
        objectSet.add(object);
    }

    class MethodProcessor {
        Class<?> cls;
        int messageId;
        Method method;
        boolean isAsync;
        Class<?> parameterCls;

        @Override
        public boolean equals(Object o) {
            if (o instanceof MethodProcessor) {
                return cls.equals(o.getClass()) && method.equals(((MethodProcessor) o).method);
            }
            return false;
        }

    }

    @Override
    public synchronized void release() {
        mHandler.removeCallbacksAndMessages(null);
        isReleased = true;
        mExecutorService.shutdownNow();
        mClassCache.clear();
        mObjectCache.clear();
        mProcessorCache.clear();
    }
}
