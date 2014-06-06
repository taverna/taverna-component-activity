package net.sf.taverna.t2.component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.api.Version.ID;
import net.sf.taverna.t2.component.registry.ComponentDataflowCache;
import net.sf.taverna.t2.component.registry.ComponentUtil;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityFactory;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityOutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;

import org.springframework.beans.factory.annotation.Required;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ComponentActivityFactory implements ActivityFactory {
	private ComponentUtil util;
	private ComponentDataflowCache cache;
	private Edits edits;

	@Override
	public Activity<?> createActivity() {
		return new ComponentActivity(util, cache, edits);
	}

	@Override
	public URI getActivityType() {
		return URI.create(ComponentActivity.URI);
	}

	@Override
	public JsonNode getActivityConfigurationSchema() {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
 			return objectMapper.readTree(getClass().getResource("/schema.json"));
		} catch (IOException e) {
			return objectMapper.createObjectNode();
		}
	}

	@Override
	public Set<ActivityInputPort> getInputPorts(JsonNode configuration)
			throws ActivityConfigurationException {
		try {
			Set<ActivityInputPort> activityInputPorts = new HashSet<>();
			for (ActivityInputPortDefinitionBean ipd : createConfiguration(
					configuration).getPorts().getInputPortDefinitions())
				activityInputPorts.add(edits.createActivityInputPort(
						ipd.getName(), ipd.getDepth(), true, null,
						ipd.getTranslatedElementType()));
			return activityInputPorts;
		} catch (MalformedURLException | RegistryException | RuntimeException e) {
			throw new ActivityConfigurationException(
					"failed to get implementation for configuration of inputs",
					e);
		}
	}

	@Override
	public Set<ActivityOutputPort> getOutputPorts(JsonNode configuration)
			throws ActivityConfigurationException {
		try {
			Set<ActivityOutputPort> activityOutputPorts = new HashSet<>();
			for (ActivityOutputPortDefinitionBean opd : createConfiguration(
					configuration).getPorts().getOutputPortDefinitions())
				activityOutputPorts.add(edits.createActivityOutputPort(
						opd.getName(), opd.getDepth(), opd.getGranularDepth()));
			return activityOutputPorts;
		} catch (MalformedURLException | RegistryException | RuntimeException e) {
			throw new ActivityConfigurationException(
					"failed to get implementation for configuration of outputs",
					e);
		}
	}

	public ComponentActivityConfigurationBean createConfiguration(ID id) {
		return new ComponentActivityConfigurationBean(id, util, cache);
	}

	public ComponentActivityConfigurationBean createConfiguration(JsonNode json)
			throws MalformedURLException {
		return new ComponentActivityConfigurationBean(json, util, cache);
	}

	@Required
	public void setComponentUtil(ComponentUtil util) {
		this.util = util;
	}
	@Required
	public void setDataflowCache(ComponentDataflowCache cache) {
		this.cache = cache;
	}
	@Required
	public void setEdits(Edits edits) {
		this.edits = edits;
	}
}