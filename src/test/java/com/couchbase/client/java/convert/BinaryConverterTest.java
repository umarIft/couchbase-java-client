/**
 * Copyright (C) 2014 Couchbase, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALING
 * IN THE SOFTWARE.
 */
package com.couchbase.client.java.convert;

import com.couchbase.client.core.message.document.CoreDocument;
import com.couchbase.client.java.convert.example.BinaryNonSerializableSampleClass;
import com.couchbase.client.java.convert.example.BinarySerializableSampleClass;
import com.couchbase.client.java.document.BinaryDocument;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests which verify the functionality for the {@link BinaryConverter}.
 *
 * @author David Sondermann
 * @since 2.0
 */
public class BinaryConverterTest {

  private BinaryConverter converter;

  @Before
  public void setup() {
    converter = new BinaryConverter();
  }

  @Test
  public void shouldConvertString() {
    final String originalBinaryObject = "Hello World!";

    final CoreDocument coreDocument = converter.encode(BinaryDocument.create(null, originalBinaryObject));
    final String decodedBinaryObject = (String) converter.decode(coreDocument).content();

    assertEquals(originalBinaryObject, decodedBinaryObject);
  }

  @Test
  public void shouldConvertStringUnicode() {
    final String originalBinaryObject = "Couchbaseのは、高速でスケーラブルなNoSQLのデータベースです。";

    final CoreDocument coreDocument = converter.encode(BinaryDocument.create(null, originalBinaryObject));
    final String decodedBinaryObject = (String) converter.decode(coreDocument).content();

    assertEquals(originalBinaryObject, decodedBinaryObject);
  }

  @Test
  public void shouldConvertBooleanTrue() {
    final boolean originalBinaryObject = true;

    final CoreDocument coreDocument = converter.encode(BinaryDocument.create(null, originalBinaryObject));
    final Boolean decodedBinaryObject = (Boolean) converter.decode(coreDocument).content();

    assertTrue(decodedBinaryObject);
  }

  @Test
  public void shouldConvertBooleanFalse() {
    final boolean originalBinaryObject = false;

    final CoreDocument coreDocument = converter.encode(BinaryDocument.create(null, originalBinaryObject));
    final Boolean decodedBinaryObject = (Boolean) converter.decode(coreDocument).content();

    assertFalse(decodedBinaryObject);
  }

  @Test
  public void shouldConvertIntegerMin() {
    final int originalBinaryObject = Integer.MIN_VALUE;

    final CoreDocument coreDocument = converter.encode(BinaryDocument.create(null, originalBinaryObject));
    final int decodedBinaryObject = (Integer) converter.decode(coreDocument).content();

    assertEquals(originalBinaryObject, decodedBinaryObject);
  }

  @Test
  public void shouldConvertIntegerMax() {
    final int originalBinaryObject = Integer.MAX_VALUE;

    final CoreDocument coreDocument = converter.encode(BinaryDocument.create(null, originalBinaryObject));
    final int decodedBinaryObject = (Integer) converter.decode(coreDocument).content();

    assertEquals(originalBinaryObject, decodedBinaryObject);
  }

  @Test
  public void shouldConvertIntegerZero() {
    final int originalBinaryObject = 0;

    final CoreDocument coreDocument = converter.encode(BinaryDocument.create(null, originalBinaryObject));
    final int decodedBinaryObject = (Integer) converter.decode(coreDocument).content();

    assertEquals(originalBinaryObject, decodedBinaryObject);
  }

  @Test
  public void shouldConvertIntegerPositive() {
    final int originalBinaryObject = 423154542;

    final CoreDocument coreDocument = converter.encode(BinaryDocument.create(null, originalBinaryObject));
    final int decodedBinaryObject = (Integer) converter.decode(coreDocument).content();

    assertEquals(originalBinaryObject, decodedBinaryObject);
  }

  @Test
  public void shouldConvertIntegerNegative() {
    final int originalBinaryObject = -786454424;

    final CoreDocument coreDocument = converter.encode(BinaryDocument.create(null, originalBinaryObject));
    final int decodedBinaryObject = (Integer) converter.decode(coreDocument).content();

    assertEquals(originalBinaryObject, decodedBinaryObject);
  }

  @Test
  public void shouldConvertLongMin() {
    final long originalBinaryObject = Long.MIN_VALUE;

    final CoreDocument coreDocument = converter.encode(BinaryDocument.create(null, originalBinaryObject));
    final long decodedBinaryObject = (Long) converter.decode(coreDocument).content();

    assertEquals(originalBinaryObject, decodedBinaryObject);
  }

  @Test
  public void shouldConvertLongMax() {
    final long originalBinaryObject = Long.MAX_VALUE;

    final CoreDocument coreDocument = converter.encode(BinaryDocument.create(null, originalBinaryObject));
    final long decodedBinaryObject = (Long) converter.decode(coreDocument).content();

    assertEquals(originalBinaryObject, decodedBinaryObject);
  }

  @Test
  public void shouldConvertLongZero() {
    final long originalBinaryObject = 0L;

    final CoreDocument coreDocument = converter.encode(BinaryDocument.create(null, originalBinaryObject));
    final long decodedBinaryObject = (Long) converter.decode(coreDocument).content();

    assertEquals(originalBinaryObject, decodedBinaryObject);
  }

  @Test
  public void shouldConvertLongPositive() {
    final long originalBinaryObject = 451534531545354152L;

    final CoreDocument coreDocument = converter.encode(BinaryDocument.create(null, originalBinaryObject));
    final long decodedBinaryObject = (Long) converter.decode(coreDocument).content();

    assertEquals(originalBinaryObject, decodedBinaryObject);
  }

  @Test
  public void shouldConvertLongNegative() {
    final long originalBinaryObject = -878448315451535454L;

    final CoreDocument coreDocument = converter.encode(BinaryDocument.create(null, originalBinaryObject));
    final long decodedBinaryObject = (Long) converter.decode(coreDocument).content();

    assertEquals(originalBinaryObject, decodedBinaryObject);
  }

  @Test
  public void shouldConvertDateNow() {
    final Date originalBinaryObject = new Date();

    final CoreDocument coreDocument = converter.encode(BinaryDocument.create(null, originalBinaryObject));
    final Date decodedBinaryObject = (Date) converter.decode(coreDocument).content();

    assertEquals(originalBinaryObject, decodedBinaryObject);
  }

  @Test
  public void shouldConvertDateZero() {
    final Date originalBinaryObject = new Date(0);

    final CoreDocument coreDocument = converter.encode(BinaryDocument.create(null, originalBinaryObject));
    final Date decodedBinaryObject = (Date) converter.decode(coreDocument).content();

    assertEquals(originalBinaryObject, decodedBinaryObject);
  }

  @Test
  public void shouldConvertDatePositive() {
    final Date originalBinaryObject = new Date(1731245321);

    final CoreDocument coreDocument = converter.encode(BinaryDocument.create(null, originalBinaryObject));
    final Date decodedBinaryObject = (Date) converter.decode(coreDocument).content();

    assertEquals(originalBinaryObject, decodedBinaryObject);
  }

  @Test
  public void shouldConvertByteMin() {
    final byte originalBinaryObject = Byte.MIN_VALUE;

    final CoreDocument coreDocument = converter.encode(BinaryDocument.create(null, originalBinaryObject));
    final byte decodedBinaryObject = (Byte) converter.decode(coreDocument).content();

    assertEquals(originalBinaryObject, decodedBinaryObject);
  }

  @Test
  public void shouldConvertByteMax() {
    final byte originalBinaryObject = Byte.MAX_VALUE;

    final CoreDocument coreDocument = converter.encode(BinaryDocument.create(null, originalBinaryObject));
    final byte decodedBinaryObject = (Byte) converter.decode(coreDocument).content();

    assertEquals(originalBinaryObject, decodedBinaryObject);
  }

  @Test
  public void shouldConvertByteZero() {
    final byte originalBinaryObject = 0;

    final CoreDocument coreDocument = converter.encode(BinaryDocument.create(null, originalBinaryObject));
    final byte decodedBinaryObject = (Byte) converter.decode(coreDocument).content();

    assertEquals(originalBinaryObject, decodedBinaryObject);
  }

  @Test
  public void shouldConvertBytePositive() {
    final byte originalBinaryObject = 23;

    final CoreDocument coreDocument = converter.encode(BinaryDocument.create(null, originalBinaryObject));
    final byte decodedBinaryObject = (Byte) converter.decode(coreDocument).content();

    assertEquals(originalBinaryObject, decodedBinaryObject);
  }

  @Test
  public void shouldConvertByteNegative() {
    final byte originalBinaryObject = -42;

    final CoreDocument coreDocument = converter.encode(BinaryDocument.create(null, originalBinaryObject));
    final byte decodedBinaryObject = (Byte) converter.decode(coreDocument).content();

    assertEquals(originalBinaryObject, decodedBinaryObject);
  }

  @Test
  public void shouldConvertFloatMin() {
    final float originalBinaryObject = Float.MIN_VALUE;

    final CoreDocument coreDocument = converter.encode(BinaryDocument.create(null, originalBinaryObject));
    final float decodedBinaryObject = (Float) converter.decode(coreDocument).content();

    assertEquals(originalBinaryObject, decodedBinaryObject, 0);
  }

  @Test
  public void shouldConvertFloatMax() {
    final float originalBinaryObject = Float.MAX_VALUE;

    final CoreDocument coreDocument = converter.encode(BinaryDocument.create(null, originalBinaryObject));
    final float decodedBinaryObject = (Float) converter.decode(coreDocument).content();

    assertEquals(originalBinaryObject, decodedBinaryObject, 0);
  }

  @Test
  public void shouldConvertFloatZero() {
    final float originalBinaryObject = 0f;

    final CoreDocument coreDocument = converter.encode(BinaryDocument.create(null, originalBinaryObject));
    final float decodedBinaryObject = (Float) converter.decode(coreDocument).content();

    assertEquals(originalBinaryObject, decodedBinaryObject, 0);
  }

  @Test
  public void shouldConvertFloatPositive() {
    final float originalBinaryObject = 3.14f;

    final CoreDocument coreDocument = converter.encode(BinaryDocument.create(null, originalBinaryObject));
    final float decodedBinaryObject = (Float) converter.decode(coreDocument).content();

    assertEquals(originalBinaryObject, decodedBinaryObject, 0);
  }

  @Test
  public void shouldConvertFloatNegative() {
    final float originalBinaryObject = -23.42f;

    final CoreDocument coreDocument = converter.encode(BinaryDocument.create(null, originalBinaryObject));
    final float decodedBinaryObject = (Float) converter.decode(coreDocument).content();

    assertEquals(originalBinaryObject, decodedBinaryObject, 0);
  }

  @Test
  public void shouldConvertDoubleMin() {
    final double originalBinaryObject = Double.MIN_VALUE;

    final CoreDocument coreDocument = converter.encode(BinaryDocument.create(null, originalBinaryObject));
    final double decodedBinaryObject = (Double) converter.decode(coreDocument).content();

    assertEquals(originalBinaryObject, decodedBinaryObject, 0);
  }

  @Test
  public void shouldConvertDoubleMax() {
    final double originalBinaryObject = Double.MAX_VALUE;

    final CoreDocument coreDocument = converter.encode(BinaryDocument.create(null, originalBinaryObject));
    final double decodedBinaryObject = (Double) converter.decode(coreDocument).content();

    assertEquals(originalBinaryObject, decodedBinaryObject, 0);
  }

  @Test
  public void shouldConvertDoubleZero() {
    final double originalBinaryObject = 0d;

    final CoreDocument coreDocument = converter.encode(BinaryDocument.create(null, originalBinaryObject));
    final double decodedBinaryObject = (Double) converter.decode(coreDocument).content();

    assertEquals(originalBinaryObject, decodedBinaryObject, 0);
  }

  @Test
  public void shouldConvertDoublePositive() {
    final double originalBinaryObject = 3.14e23d;

    final CoreDocument coreDocument = converter.encode(BinaryDocument.create(null, originalBinaryObject));
    final double decodedBinaryObject = (Double) converter.decode(coreDocument).content();

    assertEquals(originalBinaryObject, decodedBinaryObject, 0);
  }

  @Test
  public void shouldConvertDoubleNegative() {
    final double originalBinaryObject = -23.42e31d;

    final CoreDocument coreDocument = converter.encode(BinaryDocument.create(null, originalBinaryObject));
    final double decodedBinaryObject = (Double) converter.decode(coreDocument).content();

    assertEquals(originalBinaryObject, decodedBinaryObject, 0);
  }

  @Test
  public void shouldConvertByteArray() {
    final byte[] originalBinaryObject = new byte[]{127, 0, -5};

    final CoreDocument coreDocument = converter.encode(BinaryDocument.create(null, originalBinaryObject));
    final byte[] decodedBinaryObject = (byte[]) converter.decode(coreDocument).content();

    assertTrue(Arrays.equals(originalBinaryObject, decodedBinaryObject));
  }

  @Test
  public void shouldConvertBigByteArray() {
    final byte[] originalBinaryObject = new byte[20000];
    originalBinaryObject[0] = 50;
    originalBinaryObject[1] = 2;
    originalBinaryObject[2] = -127;
    originalBinaryObject[1546] = -4;
    originalBinaryObject[5104] = 127;

    final CoreDocument coreDocument = converter.encode(BinaryDocument.create(null, originalBinaryObject));
    final byte[] decodedBinaryObject = (byte[]) converter.decode(coreDocument).content();

    assertTrue(Arrays.equals(originalBinaryObject, decodedBinaryObject));
  }

  @Test
  public void shouldConvertBinarySerializableSampleClass() {
    final BinarySerializableSampleClass originalBinaryObject = new BinarySerializableSampleClass("foo");

    final CoreDocument coreDocument = converter.encode(BinaryDocument.create(null, originalBinaryObject));
    final Object decodedBinaryObject = converter.decode(coreDocument).content();

    assertTrue(decodedBinaryObject instanceof BinarySerializableSampleClass);
    assertEquals(originalBinaryObject.getFoo(), ((BinarySerializableSampleClass) decodedBinaryObject).getFoo());
  }

  @Test(expected = IllegalArgumentException.class)
  public void expectExceptionOnConvertBinaryNonSerializableSampleClass() {
    final BinaryNonSerializableSampleClass originalBinaryObject = new BinaryNonSerializableSampleClass();

    converter.encode(BinaryDocument.create(null, originalBinaryObject));
  }

  @Test(expected = NullPointerException.class)
  public void expectExceptionOnConvertNull() {
    converter.encode(null);
  }

}
