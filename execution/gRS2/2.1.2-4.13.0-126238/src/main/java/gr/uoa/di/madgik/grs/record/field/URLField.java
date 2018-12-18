package gr.uoa.di.madgik.grs.record.field;

import gr.uoa.di.madgik.commons.utils.ZipUtils;
import gr.uoa.di.madgik.grs.buffer.IBuffer.TransportDirective;
import gr.uoa.di.madgik.grs.buffer.IBuffer.TransportOverride;
import gr.uoa.di.madgik.grs.proxy.mirror.GRS2ProxyMirrorProtocolErrorException;
import gr.uoa.di.madgik.grs.record.GRS2RecordDefinitionException;
import gr.uoa.di.madgik.grs.record.GRS2RecordException;
import gr.uoa.di.madgik.grs.record.GRS2RecordSerializationException;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLConnection;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Field holding the payload of a URL
 * 
 * @author gpapanikos
 *
 */
public class URLField extends Field
{
	private URL payload=null;
	
	private int marshaledSize=0;
	private boolean marshaledCompleted=false;
	
	/**
	 * Creates a new instance
	 */
	public URLField(){}
	
	/**
	 * Creates a new instance
	 * 
	 * @param payload the payload of the field
	 */
	public URLField(URL payload)
	{
		this.payload=payload;
	}
	
	/**
	 * Sets the payload of the field
	 * 
	 * @param payload
	 */
	public void setPayload(URL payload)
	{
		this.payload=payload;
	}
	
	/**
	 * Retrieves the payload of the field
	 * 
	 * @return the field payload
	 * @throws GRS2RecordException 
	 */
	public URL getPayload()
	{
		return this.payload;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * @see gr.uoa.di.madgik.grs.record.field.Field#getFieldDefinition()
	 */
	public URLFieldDefinition getFieldDefinition() throws GRS2RecordDefinitionException
	{
		if(!(super.getFieldDefinition() instanceof URLFieldDefinition)) throw new GRS2RecordDefinitionException("Provided field definition is not of required type");
		return (URLFieldDefinition)super.getFieldDefinition();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see gr.uoa.di.madgik.grs.record.field.Field#isAvailable()
	 */
	@Override
	public boolean isAvailable()
	{
		return (this.marshaledCompleted || !this.isRemoteCopy());
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see gr.uoa.di.madgik.grs.record.field.Field#getInputStream()
	 */
	@Override
	public InputStream getInputStream() throws IOException
	{
		if(this.payload==null) return null;
		URLConnection urlc = this.payload.openConnection();
		return urlc.getInputStream();
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * @see gr.uoa.di.madgik.grs.record.field.Field#extendSend(java.io.DataOutput, gr.uoa.di.madgik.grs.buffer.IBuffer.TransportOverride)
	 */
	@Override
	public void extendSend(DataOutput out, TransportOverride override) throws GRS2RecordSerializationException
	{
		try
		{
			TransportDirective dir=this.resolveTransportDirective();
			if(override==TransportOverride.Override) dir=TransportDirective.Full;
			
			if(this.marshaledCompleted) throw new GRS2ProxyMirrorProtocolErrorException("More to marshal requested but full payload is already provided");
			this.record.markActivity();
			if(this.payload==null)
			{
				this.marshaledCompleted=true;
				out.writeInt(-1);
				out.writeBoolean(this.marshaledCompleted);
			}
			else
			{
				if(dir==TransportDirective.Inherit) throw new GRS2ProxyMirrorProtocolErrorException("Unsupported transport directive after resolution");
				if(dir==TransportDirective.Partial) throw new GRS2ProxyMirrorProtocolErrorException("Unsupported transport directive after resolution");
				byte[] pb=this.payload.toString().getBytes(this.getFieldDefinition().getCharset());
				if(this.getFieldDefinition().isCompress()) pb=ZipUtils.ZipBytes(pb);
				out.writeInt(pb.length);
				out.write(pb);
				marshaledSize+=pb.length;
				this.marshaledCompleted=true;
				out.writeBoolean(this.marshaledCompleted);
				this.record.markActivity();
			}
		} catch (Exception e)
		{
			throw new GRS2RecordSerializationException("unable to marshal field", e);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see gr.uoa.di.madgik.grs.record.field.Field#extendReceive(java.io.DataInput, gr.uoa.di.madgik.grs.buffer.IBuffer.TransportOverride)
	 */
	@Override
	public void extendReceive(DataInput in, TransportOverride override) throws GRS2RecordSerializationException
	{
		try
		{
			if(this.marshaledCompleted) throw new GRS2ProxyMirrorProtocolErrorException("Marshaling of field is already completed");
			TransportDirective dir=this.resolveTransportDirective();
			if(override==TransportOverride.Override) dir=TransportDirective.Full;
			
			if(dir==TransportDirective.Inherit) throw new GRS2ProxyMirrorProtocolErrorException("Unsupported transport directive after resolution");
			this.record.markActivity();
			int len=in.readInt();
			if(len<0)
			{
				this.payload=null;
				this.marshaledSize=0;
				this.marshaledCompleted=true;
				if(!in.readBoolean()) throw new GRS2ProxyMirrorProtocolErrorException("More to marshal detected alhough full payload was available");
			}
			else
			{
				byte[] pb=new byte[len];
				in.readFully(pb);
				if(this.getFieldDefinition().isCompress()) pb=ZipUtils.UnzipBytes(pb);
				this.payload=new URL(new String(pb, this.getFieldDefinition().getCharset()));
				this.marshaledSize+=len;
				this.marshaledCompleted=true;
				if(!in.readBoolean()) throw new GRS2ProxyMirrorProtocolErrorException("Partial transfer detected although full was resolved");
				this.record.markActivity();
			}
		} catch (Exception e)
		{
			throw new GRS2RecordSerializationException("unable to unmarshal field", e);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see gr.uoa.di.madgik.grs.record.field.Field#extendDispose()
	 */
	@Override
	public void extendDispose()
	{
		this.payload=null;
		this.marshaledSize=0;
		this.marshaledCompleted=false;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see gr.uoa.di.madgik.grs.record.field.Field#extendDeflate(java.io.DataOutput)
	 */
	@Override
	public void extendDeflate(DataOutput out) throws GRS2RecordSerializationException
	{
		try
		{
			out.writeUTF(this.getFieldDefinition().getCharset());
			if(this.payload==null) out.writeInt(-1);
			else
			{
				byte[] b= this.payload.toString().getBytes(this.getFieldDefinition().getCharset());
				out.writeInt(b.length);
				out.write(b);
			}
			out.writeInt(this.marshaledSize);
			out.writeBoolean(this.marshaledCompleted);
		}catch(Exception ex)
		{
			throw new GRS2RecordSerializationException("unable to deflate field", ex);
		}
	}

	@Override
	public void extendToXML(Document doc, Element element) throws GRS2RecordSerializationException {

		try {
			Element elm = null;

			elm = doc.createElement("charset");
			elm.setTextContent(this.getFieldDefinition().getCharset());
			element.appendChild(elm);

			elm = doc.createElement("isCompressed");
			elm.setTextContent(String.valueOf(this.getFieldDefinition().isCompress()));
			element.appendChild(elm);

			if (this.payload != null) {
				StringBuffer strBuf = new StringBuffer();

				byte[] b = this.payload.toString().getBytes(this.getFieldDefinition().getCharset());

				if (this.getFieldDefinition().isCompress()) {
					byte[] bb = ZipUtils.ZipBytes(b);
					strBuf.append(new String(bb));
				} else {
					strBuf.append(new String(b));
				}

				elm = doc.createElement("payload");
				elm.setTextContent(this.payload.toString());
				element.appendChild(elm);
			}

			elm = doc.createElement("marshaledSize");
			elm.setTextContent(String.valueOf(this.marshaledSize));
			element.appendChild(elm);

			elm = doc.createElement("marshaledCompleted");
			elm.setTextContent(String.valueOf(this.marshaledCompleted));
			element.appendChild(elm);
		} catch (Exception e) {
			throw new GRS2RecordSerializationException("unable to unmarshal field", e);
		}

	}

	@Override
	public void extendSendToXML(Document doc, Element element, TransportOverride override)
			throws GRS2RecordSerializationException {
		try {
			TransportDirective dir = this.resolveTransportDirective();
			if (override == TransportOverride.Override)
				dir = TransportDirective.Full;

			if (this.marshaledCompleted)
				throw new GRS2ProxyMirrorProtocolErrorException(
						"More to marshal requested but full payload is already provided");
			
			this.record.markActivity();

			Element elm = null;

//			elm = doc.createElement("charset");
//			elm.setTextContent(this.getFieldDefinition().getCharset());
//			element.appendChild(elm);
//			
//			elm = doc.createElement("isCompressed");
//			elm.setTextContent(String.valueOf(this.getFieldDefinition().isCompress()));
//			element.appendChild(elm);

			if (this.payload != null) {
				if (dir == TransportDirective.Inherit)
					throw new GRS2ProxyMirrorProtocolErrorException("Unsupported transport directive after resolution");
				if (dir == TransportDirective.Partial)
					throw new GRS2ProxyMirrorProtocolErrorException("Unsupported transport directive after resolution");

				StringBuffer strBuf = new StringBuffer();
				byte[] b = this.payload.toString().getBytes(this.getFieldDefinition().getCharset());

				if (this.getFieldDefinition().isCompress()) {
					byte[] bb = ZipUtils.ZipBytes(b);
					strBuf.append(new String(bb));
				} else {
					strBuf.append(new String(b));
				}

				
				elm = doc.createElement("payload");
				elm.setTextContent(this.payload.toString());
				element.appendChild(elm);

				marshaledSize += strBuf.length();
				this.marshaledCompleted = true;
				this.record.markActivity();
			}

			elm = doc.createElement("marshaledSize");
			elm.setTextContent(String.valueOf(this.marshaledSize));
			element.appendChild(elm);

			elm = doc.createElement("marshaledCompleted");
			elm.setTextContent(String.valueOf(this.marshaledCompleted));
			element.appendChild(elm);
		} catch (Exception e) {
			throw new GRS2RecordSerializationException("unable to unmarshal field", e);
		}
	}

	@Override
	public void extendReceiveFromXML(Element element,TransportOverride override) throws GRS2RecordSerializationException {
		try {
			if (this.marshaledCompleted)
				throw new GRS2ProxyMirrorProtocolErrorException("Marshaling of field is already completed");
			TransportDirective dir = this.resolveTransportDirective();
			if (override == TransportOverride.Override)
				dir = TransportDirective.Full;

			if (dir == TransportDirective.Inherit)
				throw new GRS2ProxyMirrorProtocolErrorException("Unsupported transport directive after resolution");
			this.record.markActivity();

			String payload = element.getElementsByTagName("payload").item(0).getTextContent();
			if (payload != null) {
				if (this.getFieldDefinition().isCompress()) {
					byte[] bb = ZipUtils.UnzipBytes(payload.getBytes(this.getFieldDefinition().getCharset()));
					this.payload = new URL(new String(bb));
				} else
					this.payload = new URL(payload);
				this.marshaledSize += payload.length();
			}

			
			this.marshaledCompleted = true;
		} catch (Exception e) {
			throw new GRS2RecordSerializationException("unable to get record from xml", e);
		}

	}
	
	@Override
	public void extendFromXML(Element element, boolean reset) throws GRS2RecordSerializationException {
		try {
			String charset = element.getElementsByTagName("charset").item(0).getTextContent();
			this.getFieldDefinition().setCharset(charset);

			Boolean isCompressed = Boolean.parseBoolean(element.getElementsByTagName("isCompressed").item(0)
					.getTextContent());
			this.getFieldDefinition().setCompress(isCompressed);

			String payload = element.getElementsByTagName("payload").item(0).getTextContent();
			if (payload != null) {
				if (isCompressed) {
					byte[] bb = ZipUtils.UnzipBytes(payload.getBytes(charset));
					this.payload = new URL(new String(bb));
				} else
					this.payload = new URL(payload);
			}

			Integer marshaledSize = Integer.parseInt(element.getElementsByTagName("marshaledSize").item(0)
					.getTextContent());
			this.marshaledSize = marshaledSize;

			Boolean marshaledCompleted = Boolean.parseBoolean(element.getElementsByTagName("marshaledCompleted")
					.item(0).getTextContent());
			this.marshaledCompleted = marshaledCompleted;

			if (reset) {
				this.marshaledSize = 0;
				this.marshaledCompleted = false;
			}
		} catch (Exception e) {
			throw new GRS2RecordSerializationException("unable to get record from xml", e);
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see gr.uoa.di.madgik.grs.record.field.Field#extendInflate(java.io.DataInput, boolean)
	 */
	@Override
	public void extendInflate(DataInput in,boolean reset) throws GRS2RecordSerializationException
	{
		try
		{
			String charset=in.readUTF();
			int len=in.readInt();
			if(len<0)this.payload=null;
			else
			{
				byte[] b=new byte[len];
				in.readFully(b);
				this.payload=new URL(new String(b, charset));
			}
			this.marshaledSize=in.readInt();
			this.marshaledCompleted=in.readBoolean();
			if(reset)
			{
				this.marshaledSize=0;
				this.marshaledCompleted=false;
			}
		}catch(Exception ex)
		{
			throw new GRS2RecordSerializationException("unable to inflate field", ex);
		}
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * @see gr.uoa.di.madgik.grs.record.field.Field#extendMakeLocal()
	 */
	protected void extendMakeLocal()
	{
		this.marshaledSize=0;
		this.marshaledCompleted=false;
	}

}
