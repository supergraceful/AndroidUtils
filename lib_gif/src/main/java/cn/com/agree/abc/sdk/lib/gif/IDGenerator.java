/**
 * Copyright 赞同科技.
 * All rights reserved.
 */
package cn.com.agree.abc.sdk.lib.gif;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author PuYun &lt;pu.yun@agree.com.cn&gt;
 * @version $Id$
 * 
 */
public class IDGenerator
{
    private IDGenerator()
    {
    }

    private static String prefix;

    private static AtomicLong seed = new AtomicLong(0);

    static
    {
        SecureRandom random = new SecureRandom();
        prefix ="gif_"+timeFormat(new Date());
        int randomInt = random.nextInt();
        seed.set(Math.abs(randomInt));
    }

    public static String nextGlobal()
    {
        return prefix
                + Long.toString(seed.incrementAndGet(), Character.MAX_RADIX);
    }

    public static String nextLocal()
    {
        return Long.toString(seed.incrementAndGet(), Character.MAX_RADIX);
    }
    
    public static String timeFormat(Date date) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMdd");
		return simpleDateFormat.format(date);
	}
}
