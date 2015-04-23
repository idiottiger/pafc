package com.pafc.library.messagebus;

/**
 * messsage bus interface <br>
 * <b> HOW TO USE ? </b> <li>1. {@link #register(Object)}, register the object which want to handle
 * message to the message bus</li> <li>2. need add the {@link MessageHandle} annotation to the
 * method, if want process message in another thread, can use {@link MessageAsyncHandle}</li>
 *
 * @author idiot2ger
 */
public interface IMessageBus {

    /**
     * register current object to the message bus
     *
     * @param object
     */
    public void register(Object object);

    /**
     * unregister the object
     *
     * @param object
     */
    public void unRegister(Object object);

    /**
     * post a message
     *
     * @param messageId
     */
    public void post(int messageId);

    /**
     * @param messageId
     * @param delay
     */
    public void post(int messageId, long delay);

    public void post(int messageId, Object object);

    public void post(int messageId, Object object, long delay);

    public void postImmediate(int messageId);

    public void postImmediate(int messageId, Object object);

    public void release();

}
