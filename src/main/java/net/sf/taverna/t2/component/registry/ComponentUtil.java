package net.sf.taverna.t2.component.registry;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.component.api.Component;
import net.sf.taverna.t2.component.api.Family;
import net.sf.taverna.t2.component.api.Profile;
import net.sf.taverna.t2.component.api.Registry;
import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.api.Version;
import net.sf.taverna.t2.component.profile.BaseProfileLocator;
import net.sf.taverna.t2.component.profile.ComponentProfile;
import net.sf.taverna.t2.component.registry.local.LocalComponentRegistryLocator;
import net.sf.taverna.t2.component.registry.standard.NewComponentRegistryLocator;

import org.springframework.beans.factory.annotation.Required;

/**
 * @author alanrw
 * @author dkf
 */
public class ComponentUtil {
	private NewComponentRegistryLocator netLocator;
	private BaseProfileLocator base;
	private LocalComponentRegistryLocator fileLocator;

	private final Map<String, Registry> cache = new HashMap<>();

	@Required
	public void setNetworkLocator(NewComponentRegistryLocator locator) {
		this.netLocator = locator;
	}

	@Required
	public void setFileLocator(LocalComponentRegistryLocator fileLocator) {
		this.fileLocator = fileLocator;
	}

	@Required
	public void setBaseLocator(BaseProfileLocator base) {
		this.base = base;
	}

	public Registry getRegistry(URL registryBase) throws RegistryException {
		Registry registry = cache.get(registryBase.toString());
		if (registry != null)
			return registry;

		if (registryBase.getProtocol().startsWith("http")) {
			if (!netLocator.verifyBase(registryBase))
				throw new RegistryException(
						"Unable to establish credentials for " + registryBase);
			registry = netLocator.getComponentRegistry(registryBase);
		} else
			registry = fileLocator.getComponentRegistry(registryBase);
		cache.put(registryBase.toString(), registry);
		return registry;
	}

	public Family getFamily(URL registryBase, String familyName)
			throws RegistryException {
		return getRegistry(registryBase).getComponentFamily(familyName);
	}

	public Component getComponent(URL registryBase, String familyName,
			String componentName) throws RegistryException {
		return getRegistry(registryBase).getComponentFamily(familyName)
				.getComponent(componentName);
	}

	public Version getVersion(URL registryBase, String familyName,
			String componentName, Integer componentVersion)
			throws RegistryException {
		return getRegistry(registryBase).getComponentFamily(familyName)
				.getComponent(componentName)
				.getComponentVersion(componentVersion);
	}

	public Version getVersion(Version.ID ident) throws RegistryException {
		return getVersion(ident.getRegistryBase(), ident.getFamilyName(),
				ident.getComponentName(), ident.getComponentVersion());
	}

	public Component getComponent(Version.ID ident) throws RegistryException {
		return getComponent(ident.getRegistryBase(), ident.getFamilyName(),
				ident.getComponentName());
	}

	public Profile getProfile(URL url) throws RegistryException {
		Profile p = new ComponentProfile(url, base);
		p.getProfileDocument(); // force immediate loading
		return p;
	}

	public Profile getBaseProfile() throws RegistryException {
		return base.getProfile();
	}

	public BaseProfileLocator getBaseProfileLocator() {
		return base;
	}
}
