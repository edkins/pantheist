package restless.common.api.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import restless.common.util.OtherPreconditions;

final class CreateActionImpl implements CreateAction
{
	private final BasicContentType basicType;
	private final String mimeType;
	private final List<AdditionalStructureItem> additionalStructure;

	@Inject
	private CreateActionImpl(
			@Assisted @JsonProperty("basicType") final BasicContentType basicType,
			@Assisted("mimeType") @JsonProperty("mimeType") final String mimeType,
			@Nullable @Assisted @JsonProperty("additionalStructure") final List<AdditionalStructureItem> additionalStructure)
	{
		this.basicType = checkNotNull(basicType);
		this.mimeType = OtherPreconditions.checkNotNullOrEmpty(mimeType);
		this.additionalStructure = additionalStructure;
	}

	@Override
	public BasicContentType basicType()
	{
		return basicType;
	}

	@Override
	public String mimeType()
	{
		return mimeType;
	}

	@Override
	public List<AdditionalStructureItem> additionalStructure()
	{
		return additionalStructure;
	}

}
