/*
 * Copyright DataStax, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datastax.oss.driver.internal.core.type.codec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.datastax.oss.driver.api.core.data.CqlVector;
import com.datastax.oss.driver.api.core.type.CqlVectorType;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.core.type.codec.TypeCodecs;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;
import org.junit.Test;

public class CqlVectorCodecTest extends CodecTestBase<CqlVector<Float>> {

  private static final CqlVector VECTOR = CqlVector.builder().add(1.0f, 2.5f).build();

  private static final String VECTOR_HEX_STRING = "0x" + "3f800000" + "40200000";

  private static final String FORMATTED_VECTOR = "[1.0, 2.5]";

  public CqlVectorCodecTest() {
    CqlVectorType vectorType = DataTypes.vectorOf(DataTypes.FLOAT, 2);
    this.codec = TypeCodecs.vectorOf(vectorType, TypeCodecs.FLOAT);
  }

  @Test
  public void should_encode() {
    assertThat(encode(VECTOR)).isEqualTo(VECTOR_HEX_STRING);
    assertThat(encode(null)).isNull();
  }

  @Test
  public void should_decode() {
    assertThat(decode(VECTOR_HEX_STRING)).isEqualTo(VECTOR);
    assertThat(decode("0x")).isNull();
    assertThat(decode(null)).isNull();
  }

  @Test
  public void decode_throws_if_too_few_bytes() {
    // Dropping 4 bytes would knock off exactly 1 float, anything less than that would be something
    // we couldn't parse a float out of
    for (int i = 1; i <= 3; ++i) {
      // 2 chars of hex encoded string = 1 byte
      int lastIndex = VECTOR_HEX_STRING.length() - (2 * i);
      assertThatThrownBy(() -> decode(VECTOR_HEX_STRING.substring(0, lastIndex)))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Test
  public void should_format() {
    assertThat(format(VECTOR)).isEqualTo(FORMATTED_VECTOR);
    assertThat(format(null)).isEqualTo("NULL");
  }

  @Test
  public void should_parse() {
    assertThat(parse(FORMATTED_VECTOR)).isEqualTo(VECTOR);
    assertThat(parse("NULL")).isNull();
    assertThat(parse("null")).isNull();
    assertThat(parse("")).isNull();
    assertThat(parse(null)).isNull();
  }

  @Test
  public void should_accept_data_type() {
    assertThat(codec.accepts(new CqlVectorType(DataTypes.FLOAT, 2))).isTrue();
    assertThat(codec.accepts(DataTypes.INT)).isFalse();
  }

  @Test
  public void should_accept_vector_type_correct_dimension_only() {
    assertThat(codec.accepts(new CqlVectorType(DataTypes.FLOAT, 0))).isFalse();
    assertThat(codec.accepts(new CqlVectorType(DataTypes.FLOAT, 1))).isFalse();
    assertThat(codec.accepts(new CqlVectorType(DataTypes.FLOAT, 2))).isTrue();
    for (int i = 3; i < 1000; ++i) {
      assertThat(codec.accepts(new CqlVectorType(DataTypes.FLOAT, i))).isFalse();
    }
  }

  @Test
  public void should_accept_generic_type() {
    assertThat(codec.accepts(GenericType.vectorOf(GenericType.FLOAT))).isTrue();
    assertThat(codec.accepts(GenericType.vectorOf(GenericType.INTEGER))).isFalse();
    assertThat(codec.accepts(GenericType.of(Integer.class))).isFalse();
  }

  @Test
  public void should_accept_raw_type() {
    assertThat(codec.accepts(CqlVector.class)).isTrue();
    assertThat(codec.accepts(Integer.class)).isFalse();
  }

  @Test
  public void should_accept_object() {
    assertThat(codec.accepts(VECTOR)).isTrue();
    assertThat(codec.accepts(Integer.MIN_VALUE)).isFalse();
  }
}
