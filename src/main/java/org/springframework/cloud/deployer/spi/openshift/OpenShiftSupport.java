package org.springframework.cloud.deployer.spi.openshift;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.deployer.spi.core.AppDeploymentRequest;
import org.springframework.util.Assert;

import io.fabric8.kubernetes.api.model.EnvVar;

public interface OpenShiftSupport {

	default String getImageTag(AppDeploymentRequest request,
			OpenShiftDeployerProperties properties, String appId) {
		return format("%s:%s", appId,
				request.getDeploymentProperties().getOrDefault(
						OpenShiftDeploymentPropertyKeys.OPENSHIFT_DEPLOYMENT_IMAGE_TAG,
						properties.getDefaultImageTag()));
	}

	default String getEnvironmentVariable(String[] properties, String name) {
		return getEnvironmentVariable(properties, name, StringUtils.EMPTY);
	}

	default String getEnvironmentVariable(String[] properties, String name,
			String defaultValue) {
		return toEnvVars(properties).stream()
				.filter(envVar -> envVar.getName().equals(name)).map(EnvVar::getValue)
				.findFirst().orElse(defaultValue);
	}

	default List<EnvVar> toEnvVars(String[] properties) {
		List<EnvVar> envVars = new ArrayList<>();
		for (String envVar : properties) {
			String[] strings = envVar.split("=", 2);
			Assert.isTrue(strings.length == 2,
					"Invalid environment variable declared: " + envVar);
			envVars.add(new EnvVar(strings[0], strings[1], null));
		}

		return envVars;
	}

	default Map<String, String> getNodeSelectors(Map<String, String> properties) {
		Map<String, String> nodeSelectors = new HashMap<>();

		String nodeSelectorsProperty = properties.getOrDefault(
				OpenShiftDeploymentPropertyKeys.OPENSHIFT_DEPLOYMENT_NODE_SELECTOR,
				StringUtils.EMPTY);

		if (StringUtils.isNotBlank(nodeSelectorsProperty)) {
			String[] nodeSelectorPairs = nodeSelectorsProperty.split(",");
			for (String nodeSelectorPair : nodeSelectorPairs) {
				String[] nodeSelector = nodeSelectorPair.split(":");
				Assert.isTrue(nodeSelector.length == 2,
						format("Invalid nodeSelector value: {}", nodeSelectorPair));

				nodeSelectors.put(nodeSelector[0].trim(), nodeSelector[1].trim());
			}
		}

		return nodeSelectors;
	}
}
