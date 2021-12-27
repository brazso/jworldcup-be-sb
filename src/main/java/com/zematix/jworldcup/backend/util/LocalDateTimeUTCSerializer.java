package com.zematix.jworldcup.backend.util;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/*
 * A Jackson serializer of LocalDateTime. The latter one does not contain timezone info,
 * but with this custom serializer UTC info is attached to the serialized string as 
 * 'Z' at the end of it.
 * Original LocalDateTimeSerializer with DateTimeFormatter.ISO_OFFSET_DATE_TIME format
 * cannot be used with LocalDateTime input.
 */
public class LocalDateTimeUTCSerializer extends StdSerializer<LocalDateTime> {

	private static final long serialVersionUID = -639333373209238925L;

	public LocalDateTimeUTCSerializer(Class<LocalDateTime> t) {
        super(t);
    }

    public LocalDateTimeUTCSerializer() {
        this(null);
    }

    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider sp)
            throws IOException {
    	if (value != null) {
    		gen.writeString(value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z");
    	}
    	else {
    		gen.writeString("null");	
    	}
    }
}