package org.runaway.example;

import org.runaway.exception.RunawayException;

public class B {

    public String calc(String paramA)
    {
        int x = 35;
        C c;

        try {
            c = new C();
            c.getData(54110, paramA);
            x = 73;
            return "abcd";
        } catch(RuntimeException e) {
            RunawayException re = RunawayException.of(e);
            re.snap("paramA", paramA);
            re.snap("x", x);
            throw re;
        }
    }

}
