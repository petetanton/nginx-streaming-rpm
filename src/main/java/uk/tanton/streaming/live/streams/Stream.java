package uk.tanton.streaming.live.streams;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Stream {
// app=app
// flashver=Wirecast/FM%201.0%20(compatible%3B%20MS
// swfurl=
// tcurl=rtmp://54.171.130.111:1935/app
// pageurl=
// addr=82.9.169.215
// clientid=1
// call=publish
// name=wirecastTest
// type=LIVE
// user=123
// password=2323

    private final String application;
    private final String name;
    private final String user;
    private final String password;

    public Stream(final String application, final String name, final String user, final String password) {
        this.application = application;
        this.name = name;
        this.user = user;
        this.password = password;
    }

    public String getApplication() {
        return application;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getUser() {
        return user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Stream stream = (Stream) o;


        return new EqualsBuilder()
                .append(application, stream.application)
                .append(name, stream.name)
                .append(user, stream.user)
                .append(password, stream.password)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(application)
                .append(name)
                .append(user)
                .append(password)
                .toHashCode();
    }
}
