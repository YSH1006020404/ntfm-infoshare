package cn.les.ntfm.util;

import cn.les.ntfm.constant.Constants;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 命名线程工厂
 *
 * @author 杨硕
 * @version 1.0
 * @date 2019-03-29 13:35
 */
public class NamedThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    public NamedThreadFactory(String pollName, String threadGroup) {
        group = new ThreadGroup(threadGroup);
        namePrefix = pollName + Constants.UNDERLINE + threadGroup + Constants.UNDERLINE;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
        if (t.isDaemon()) {

            t.setDaemon(false);
        }
        if (t.getPriority() != Thread.NORM_PRIORITY) {

            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }
}