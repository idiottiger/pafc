package com.pafc.library.messagebus;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * as same as {@link MessageHandle}, but it's async handle, will run in another thread
 * 
 * @author idiot2ger
 * 
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MessageAsyncHandle {

  /**
   * message id
   * 
   * @return
   */
  public int value() default 0;
}
