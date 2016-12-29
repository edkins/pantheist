package restless.client.api;

public interface ManagementDataSchema extends ManagementData
{
	ResponseType validate(String data, String contentType);

	String url();
}
