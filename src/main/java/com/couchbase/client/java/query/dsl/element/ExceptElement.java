package com.couchbase.client.java.query.dsl.element;

import com.couchbase.client.core.annotations.InterfaceAudience;
import com.couchbase.client.core.annotations.InterfaceStability;
import com.couchbase.client.deps.io.netty.util.internal.StringUtil;
import com.couchbase.client.java.query.Statement;

@InterfaceStability.Experimental
@InterfaceAudience.Private
public class ExceptElement implements Element {
    private final boolean all;
    private final String with;

    public ExceptElement(final boolean all) {
        this.all = all;
        this.with = null;
    }

    public ExceptElement(final boolean all, final String with) {
        this.all = all;
        this.with = with;
    }

    public ExceptElement(final boolean all, final Statement with) {
        this.all = all;
        this.with = with.toString();
    }

    @Override
    public String export() {
        final StringBuilder sb = new StringBuilder();

        sb.append("EXCEPT");

        if (all) {
            sb.append(" ALL");
        }

        if (!StringUtil.isNullOrEmpty(with)) {
            sb.append(" ");
            sb.append(with);
        }

        return sb.toString();

    }
}
