package org.ehcache.demo.serializer;

import org.ehcache.demo.model.Employee;
import org.ehcache.spi.serialization.Serializer;
import org.ehcache.spi.serialization.SerializerException;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInputStream;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by alsu on 22/09/15.
 */
// tag::thirdPartySerializer[]
public class KryoSerializer implements Serializer<Employee> {

  private static final Kryo kryo = new Kryo();

  public KryoSerializer(ClassLoader loader) {
    //no-op
  }

  @Override
  public ByteBuffer serialize(final Employee object) throws SerializerException {
    Output output = new Output(new ByteArrayOutputStream());
    kryo.writeObject(output, object);
    output.close();

    return ByteBuffer.wrap(output.getBuffer());
  }

  @Override
  public Employee read(final ByteBuffer binary) throws ClassNotFoundException, SerializerException {
    Input input =  new Input(new ByteBufferInputStream(binary)) ;
    return kryo.readObject(input, Employee.class);
  }

  @Override
  public boolean equals(final Employee object, final ByteBuffer binary) throws ClassNotFoundException, SerializerException {
    return object.equals(read(binary));
  }

}
// end::thirdPartySerializer[]
