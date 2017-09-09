/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package io.confluent.connect.avro;

import org.apache.avro.specific.SpecificData;
import org.apache.avro.message.BinaryMessageEncoder;
import org.apache.avro.message.BinaryMessageDecoder;
import org.apache.avro.message.SchemaStore;

@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class ConnectDefault extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  private static final long serialVersionUID = 5999763488128437543L;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"ConnectDefault\",\"namespace\":\"io.confluent.connect.avro\",\"fields\":[{\"name\":\"id\",\"type\":\"int\"},{\"name\":\"sales_person\",\"type\":\"string\"},{\"name\":\"first_name\",\"type\":\"string\"},{\"name\":\"last_name\",\"type\":\"string\"},{\"name\":\"total_sale\",\"type\":{\"type\":\"bytes\",\"scale\":2,\"precision\":64,\"connect.version\":1,\"connect.parameters\":{\"scale\":\"2\"},\"connect.name\":\"org.apache.kafka.connect.data.Decimal\",\"logicalType\":\"decimal\"}},{\"name\":\"transaction_date\",\"type\":{\"type\":\"long\",\"connect.version\":1,\"connect.name\":\"org.apache.kafka.connect.data.Timestamp\",\"logicalType\":\"timestamp-millis\"}}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }

  private static SpecificData MODEL$ = new SpecificData();

  private static final BinaryMessageEncoder<ConnectDefault> ENCODER =
      new BinaryMessageEncoder<ConnectDefault>(MODEL$, SCHEMA$);

  private static final BinaryMessageDecoder<ConnectDefault> DECODER =
      new BinaryMessageDecoder<ConnectDefault>(MODEL$, SCHEMA$);

  /**
   * Return the BinaryMessageDecoder instance used by this class.
   */
  public static BinaryMessageDecoder<ConnectDefault> getDecoder() {
    return DECODER;
  }

  /**
   * Create a new BinaryMessageDecoder instance for this class that uses the specified {@link SchemaStore}.
   * @param resolver a {@link SchemaStore} used to find schemas by fingerprint
   */
  public static BinaryMessageDecoder<ConnectDefault> createDecoder(SchemaStore resolver) {
    return new BinaryMessageDecoder<ConnectDefault>(MODEL$, SCHEMA$, resolver);
  }

  /** Serializes this ConnectDefault to a ByteBuffer. */
  public java.nio.ByteBuffer toByteBuffer() throws java.io.IOException {
    return ENCODER.encode(this);
  }

  /** Deserializes a ConnectDefault from a ByteBuffer. */
  public static ConnectDefault fromByteBuffer(
      java.nio.ByteBuffer b) throws java.io.IOException {
    return DECODER.decode(b);
  }

  @Deprecated public int id;
  @Deprecated public java.lang.CharSequence sales_person;
  @Deprecated public java.lang.CharSequence first_name;
  @Deprecated public java.lang.CharSequence last_name;
  @Deprecated public java.nio.ByteBuffer total_sale;
  @Deprecated public org.joda.time.DateTime transaction_date;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>.
   */
  public ConnectDefault() {}

  /**
   * All-args constructor.
   * @param id The new value for id
   * @param sales_person The new value for sales_person
   * @param first_name The new value for first_name
   * @param last_name The new value for last_name
   * @param total_sale The new value for total_sale
   * @param transaction_date The new value for transaction_date
   */
  public ConnectDefault(java.lang.Integer id, java.lang.CharSequence sales_person, java.lang.CharSequence first_name, java.lang.CharSequence last_name, java.nio.ByteBuffer total_sale, org.joda.time.DateTime transaction_date) {
    this.id = id;
    this.sales_person = sales_person;
    this.first_name = first_name;
    this.last_name = last_name;
    this.total_sale = total_sale;
    this.transaction_date = transaction_date;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call.
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return id;
    case 1: return sales_person;
    case 2: return first_name;
    case 3: return last_name;
    case 4: return total_sale;
    case 5: return transaction_date;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  protected static final org.apache.avro.data.TimeConversions.DateConversion DATE_CONVERSION = new org.apache.avro.data.TimeConversions.DateConversion();
  protected static final org.apache.avro.data.TimeConversions.TimeConversion TIME_CONVERSION = new org.apache.avro.data.TimeConversions.TimeConversion();
  protected static final org.apache.avro.data.TimeConversions.TimestampConversion TIMESTAMP_CONVERSION = new org.apache.avro.data.TimeConversions.TimestampConversion();
  protected static final org.apache.avro.Conversions.DecimalConversion DECIMAL_CONVERSION = new org.apache.avro.Conversions.DecimalConversion();

  private static final org.apache.avro.Conversion<?>[] conversions =
      new org.apache.avro.Conversion<?>[] {
      null,
      null,
      null,
      null,
      null,
      TIMESTAMP_CONVERSION,
      null
  };

  @Override
  public org.apache.avro.Conversion<?> getConversion(int field) {
    return conversions[field];
  }

  // Used by DatumReader.  Applications should not call.
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: id = (java.lang.Integer)value$; break;
    case 1: sales_person = (java.lang.CharSequence)value$; break;
    case 2: first_name = (java.lang.CharSequence)value$; break;
    case 3: last_name = (java.lang.CharSequence)value$; break;
    case 4: total_sale = (java.nio.ByteBuffer)value$; break;
    case 5: transaction_date = (org.joda.time.DateTime)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'id' field.
   * @return The value of the 'id' field.
   */
  public java.lang.Integer getId() {
    return id;
  }

  /**
   * Sets the value of the 'id' field.
   * @param value the value to set.
   */
  public void setId(java.lang.Integer value) {
    this.id = value;
  }

  /**
   * Gets the value of the 'sales_person' field.
   * @return The value of the 'sales_person' field.
   */
  public java.lang.CharSequence getSalesPerson() {
    return sales_person;
  }

  /**
   * Sets the value of the 'sales_person' field.
   * @param value the value to set.
   */
  public void setSalesPerson(java.lang.CharSequence value) {
    this.sales_person = value;
  }

  /**
   * Gets the value of the 'first_name' field.
   * @return The value of the 'first_name' field.
   */
  public java.lang.CharSequence getFirstName() {
    return first_name;
  }

  /**
   * Sets the value of the 'first_name' field.
   * @param value the value to set.
   */
  public void setFirstName(java.lang.CharSequence value) {
    this.first_name = value;
  }

  /**
   * Gets the value of the 'last_name' field.
   * @return The value of the 'last_name' field.
   */
  public java.lang.CharSequence getLastName() {
    return last_name;
  }

  /**
   * Sets the value of the 'last_name' field.
   * @param value the value to set.
   */
  public void setLastName(java.lang.CharSequence value) {
    this.last_name = value;
  }

  /**
   * Gets the value of the 'total_sale' field.
   * @return The value of the 'total_sale' field.
   */
  public java.nio.ByteBuffer getTotalSale() {
    return total_sale;
  }

  /**
   * Sets the value of the 'total_sale' field.
   * @param value the value to set.
   */
  public void setTotalSale(java.nio.ByteBuffer value) {
    this.total_sale = value;
  }

  /**
   * Gets the value of the 'transaction_date' field.
   * @return The value of the 'transaction_date' field.
   */
  public org.joda.time.DateTime getTransactionDate() {
    return transaction_date;
  }

  /**
   * Sets the value of the 'transaction_date' field.
   * @param value the value to set.
   */
  public void setTransactionDate(org.joda.time.DateTime value) {
    this.transaction_date = value;
  }

  /**
   * Creates a new ConnectDefault RecordBuilder.
   * @return A new ConnectDefault RecordBuilder
   */
  public static io.confluent.connect.avro.ConnectDefault.Builder newBuilder() {
    return new io.confluent.connect.avro.ConnectDefault.Builder();
  }

  /**
   * Creates a new ConnectDefault RecordBuilder by copying an existing Builder.
   * @param other The existing builder to copy.
   * @return A new ConnectDefault RecordBuilder
   */
  public static io.confluent.connect.avro.ConnectDefault.Builder newBuilder(io.confluent.connect.avro.ConnectDefault.Builder other) {
    return new io.confluent.connect.avro.ConnectDefault.Builder(other);
  }

  /**
   * Creates a new ConnectDefault RecordBuilder by copying an existing ConnectDefault instance.
   * @param other The existing instance to copy.
   * @return A new ConnectDefault RecordBuilder
   */
  public static io.confluent.connect.avro.ConnectDefault.Builder newBuilder(io.confluent.connect.avro.ConnectDefault other) {
    return new io.confluent.connect.avro.ConnectDefault.Builder(other);
  }

  /**
   * RecordBuilder for ConnectDefault instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<ConnectDefault>
    implements org.apache.avro.data.RecordBuilder<ConnectDefault> {

    private int id;
    private java.lang.CharSequence sales_person;
    private java.lang.CharSequence first_name;
    private java.lang.CharSequence last_name;
    private java.nio.ByteBuffer total_sale;
    private org.joda.time.DateTime transaction_date;

    /** Creates a new Builder */
    private Builder() {
      super(SCHEMA$);
    }

    /**
     * Creates a Builder by copying an existing Builder.
     * @param other The existing Builder to copy.
     */
    private Builder(io.confluent.connect.avro.ConnectDefault.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.id)) {
        this.id = data().deepCopy(fields()[0].schema(), other.id);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.sales_person)) {
        this.sales_person = data().deepCopy(fields()[1].schema(), other.sales_person);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.first_name)) {
        this.first_name = data().deepCopy(fields()[2].schema(), other.first_name);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.last_name)) {
        this.last_name = data().deepCopy(fields()[3].schema(), other.last_name);
        fieldSetFlags()[3] = true;
      }
      if (isValidValue(fields()[4], other.total_sale)) {
        this.total_sale = data().deepCopy(fields()[4].schema(), other.total_sale);
        fieldSetFlags()[4] = true;
      }
      if (isValidValue(fields()[5], other.transaction_date)) {
        this.transaction_date = data().deepCopy(fields()[5].schema(), other.transaction_date);
        fieldSetFlags()[5] = true;
      }
    }

    /**
     * Creates a Builder by copying an existing ConnectDefault instance
     * @param other The existing instance to copy.
     */
    private Builder(io.confluent.connect.avro.ConnectDefault other) {
            super(SCHEMA$);
      if (isValidValue(fields()[0], other.id)) {
        this.id = data().deepCopy(fields()[0].schema(), other.id);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.sales_person)) {
        this.sales_person = data().deepCopy(fields()[1].schema(), other.sales_person);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.first_name)) {
        this.first_name = data().deepCopy(fields()[2].schema(), other.first_name);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.last_name)) {
        this.last_name = data().deepCopy(fields()[3].schema(), other.last_name);
        fieldSetFlags()[3] = true;
      }
      if (isValidValue(fields()[4], other.total_sale)) {
        this.total_sale = data().deepCopy(fields()[4].schema(), other.total_sale);
        fieldSetFlags()[4] = true;
      }
      if (isValidValue(fields()[5], other.transaction_date)) {
        this.transaction_date = data().deepCopy(fields()[5].schema(), other.transaction_date);
        fieldSetFlags()[5] = true;
      }
    }

    /**
      * Gets the value of the 'id' field.
      * @return The value.
      */
    public java.lang.Integer getId() {
      return id;
    }

    /**
      * Sets the value of the 'id' field.
      * @param value The value of 'id'.
      * @return This builder.
      */
    public io.confluent.connect.avro.ConnectDefault.Builder setId(int value) {
      validate(fields()[0], value);
      this.id = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /**
      * Checks whether the 'id' field has been set.
      * @return True if the 'id' field has been set, false otherwise.
      */
    public boolean hasId() {
      return fieldSetFlags()[0];
    }


    /**
      * Clears the value of the 'id' field.
      * @return This builder.
      */
    public io.confluent.connect.avro.ConnectDefault.Builder clearId() {
      fieldSetFlags()[0] = false;
      return this;
    }

    /**
      * Gets the value of the 'sales_person' field.
      * @return The value.
      */
    public java.lang.CharSequence getSalesPerson() {
      return sales_person;
    }

    /**
      * Sets the value of the 'sales_person' field.
      * @param value The value of 'sales_person'.
      * @return This builder.
      */
    public io.confluent.connect.avro.ConnectDefault.Builder setSalesPerson(java.lang.CharSequence value) {
      validate(fields()[1], value);
      this.sales_person = value;
      fieldSetFlags()[1] = true;
      return this;
    }

    /**
      * Checks whether the 'sales_person' field has been set.
      * @return True if the 'sales_person' field has been set, false otherwise.
      */
    public boolean hasSalesPerson() {
      return fieldSetFlags()[1];
    }


    /**
      * Clears the value of the 'sales_person' field.
      * @return This builder.
      */
    public io.confluent.connect.avro.ConnectDefault.Builder clearSalesPerson() {
      sales_person = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    /**
      * Gets the value of the 'first_name' field.
      * @return The value.
      */
    public java.lang.CharSequence getFirstName() {
      return first_name;
    }

    /**
      * Sets the value of the 'first_name' field.
      * @param value The value of 'first_name'.
      * @return This builder.
      */
    public io.confluent.connect.avro.ConnectDefault.Builder setFirstName(java.lang.CharSequence value) {
      validate(fields()[2], value);
      this.first_name = value;
      fieldSetFlags()[2] = true;
      return this;
    }

    /**
      * Checks whether the 'first_name' field has been set.
      * @return True if the 'first_name' field has been set, false otherwise.
      */
    public boolean hasFirstName() {
      return fieldSetFlags()[2];
    }


    /**
      * Clears the value of the 'first_name' field.
      * @return This builder.
      */
    public io.confluent.connect.avro.ConnectDefault.Builder clearFirstName() {
      first_name = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    /**
      * Gets the value of the 'last_name' field.
      * @return The value.
      */
    public java.lang.CharSequence getLastName() {
      return last_name;
    }

    /**
      * Sets the value of the 'last_name' field.
      * @param value The value of 'last_name'.
      * @return This builder.
      */
    public io.confluent.connect.avro.ConnectDefault.Builder setLastName(java.lang.CharSequence value) {
      validate(fields()[3], value);
      this.last_name = value;
      fieldSetFlags()[3] = true;
      return this;
    }

    /**
      * Checks whether the 'last_name' field has been set.
      * @return True if the 'last_name' field has been set, false otherwise.
      */
    public boolean hasLastName() {
      return fieldSetFlags()[3];
    }


    /**
      * Clears the value of the 'last_name' field.
      * @return This builder.
      */
    public io.confluent.connect.avro.ConnectDefault.Builder clearLastName() {
      last_name = null;
      fieldSetFlags()[3] = false;
      return this;
    }

    /**
      * Gets the value of the 'total_sale' field.
      * @return The value.
      */
    public java.nio.ByteBuffer getTotalSale() {
      return total_sale;
    }

    /**
      * Sets the value of the 'total_sale' field.
      * @param value The value of 'total_sale'.
      * @return This builder.
      */
    public io.confluent.connect.avro.ConnectDefault.Builder setTotalSale(java.nio.ByteBuffer value) {
      validate(fields()[4], value);
      this.total_sale = value;
      fieldSetFlags()[4] = true;
      return this;
    }

    /**
      * Checks whether the 'total_sale' field has been set.
      * @return True if the 'total_sale' field has been set, false otherwise.
      */
    public boolean hasTotalSale() {
      return fieldSetFlags()[4];
    }


    /**
      * Clears the value of the 'total_sale' field.
      * @return This builder.
      */
    public io.confluent.connect.avro.ConnectDefault.Builder clearTotalSale() {
      total_sale = null;
      fieldSetFlags()[4] = false;
      return this;
    }

    /**
      * Gets the value of the 'transaction_date' field.
      * @return The value.
      */
    public org.joda.time.DateTime getTransactionDate() {
      return transaction_date;
    }

    /**
      * Sets the value of the 'transaction_date' field.
      * @param value The value of 'transaction_date'.
      * @return This builder.
      */
    public io.confluent.connect.avro.ConnectDefault.Builder setTransactionDate(org.joda.time.DateTime value) {
      validate(fields()[5], value);
      this.transaction_date = value;
      fieldSetFlags()[5] = true;
      return this;
    }

    /**
      * Checks whether the 'transaction_date' field has been set.
      * @return True if the 'transaction_date' field has been set, false otherwise.
      */
    public boolean hasTransactionDate() {
      return fieldSetFlags()[5];
    }


    /**
      * Clears the value of the 'transaction_date' field.
      * @return This builder.
      */
    public io.confluent.connect.avro.ConnectDefault.Builder clearTransactionDate() {
      fieldSetFlags()[5] = false;
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ConnectDefault build() {
      try {
        ConnectDefault record = new ConnectDefault();
        record.id = fieldSetFlags()[0] ? this.id : (java.lang.Integer) defaultValue(fields()[0], record.getConversion(0));
        record.sales_person = fieldSetFlags()[1] ? this.sales_person : (java.lang.CharSequence) defaultValue(fields()[1], record.getConversion(1));
        record.first_name = fieldSetFlags()[2] ? this.first_name : (java.lang.CharSequence) defaultValue(fields()[2], record.getConversion(2));
        record.last_name = fieldSetFlags()[3] ? this.last_name : (java.lang.CharSequence) defaultValue(fields()[3], record.getConversion(3));
        record.total_sale = fieldSetFlags()[4] ? this.total_sale : (java.nio.ByteBuffer) defaultValue(fields()[4], record.getConversion(4));
        record.transaction_date = fieldSetFlags()[5] ? this.transaction_date : (org.joda.time.DateTime) defaultValue(fields()[5], record.getConversion(5));
        return record;
      } catch (java.lang.Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumWriter<ConnectDefault>
    WRITER$ = (org.apache.avro.io.DatumWriter<ConnectDefault>)MODEL$.createDatumWriter(SCHEMA$);

  @Override public void writeExternal(java.io.ObjectOutput out)
    throws java.io.IOException {
    WRITER$.write(this, SpecificData.getEncoder(out));
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumReader<ConnectDefault>
    READER$ = (org.apache.avro.io.DatumReader<ConnectDefault>)MODEL$.createDatumReader(SCHEMA$);

  @Override public void readExternal(java.io.ObjectInput in)
    throws java.io.IOException {
    READER$.read(this, SpecificData.getDecoder(in));
  }

}
