package org.runaway.example;

import java.sql.SQLException;

import org.runaway.exception.RunawayException;

public class C {

    public String getData(long paramA, String paramB)
    {
        String data = null;
        int jj = 13;

        try {
            jj = 159;
            if (jj == 159)
                throw new SQLException("Bad JDBC connection");

        data = "item345";
        return data;
    } catch (RuntimeException | SQLException e) {
      RunawayException re = RunawayException.of(e);
        re.snap("paramA", paramA);
        re.snap("paramB", paramB);
        re.snap("data", data);
        re.snap("jj", jj);
        throw re;
        }
    }
}
