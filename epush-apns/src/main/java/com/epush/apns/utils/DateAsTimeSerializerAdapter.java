package com.epush.apns.utils;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * <一句话功能简述> <功能详细描述>
 *
 * @author guofazhan
 * @version [版本号, 2017/2/28]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class DateAsTimeSerializerAdapter
		implements JsonSerializer<Date>, JsonDeserializer<Date> {

	private final TimeUnit timeUnit;

	public DateAsTimeSerializerAdapter(final TimeUnit timeUnit) {
		Objects.requireNonNull(timeUnit);
		this.timeUnit = timeUnit;
	}

	@Override
	public Date deserialize(final JsonElement json, final Type typeOfT,
			final JsonDeserializationContext context)
			throws JsonParseException {
		final Date date;

		if (json.isJsonPrimitive()) {
			date = new Date(this.timeUnit.toMillis(json.getAsLong()));
		} else if (json.isJsonNull()) {
			date = null;
		} else {
			throw new JsonParseException(
					"Dates represented as time since the epoch must either be numbers or null.");
		}

		return date;
	}

	@Override
	public JsonElement serialize(final Date src, final Type typeOfSrc,
			final JsonSerializationContext context) {
		final JsonElement element;

		if (src != null) {
			element = new JsonPrimitive(this.timeUnit.convert(src.getTime(),
					TimeUnit.MILLISECONDS));
		} else {
			element = JsonNull.INSTANCE;
		}

		return element;
	}
}
