package net.sf.taverna.t2.component;

import static org.apache.log4j.Logger.getLogger;

import java.util.Map;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.profile.ExceptionHandling;
import net.sf.taverna.t2.component.registry.ComponentUtil;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.impl.InvocationContextImpl;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;
import net.sf.taverna.t2.workflowmodel.processor.activity.LockedNestedDataflow;
import net.sf.taverna.t2.workflowmodel.utils.AnnotationTools;

import org.apache.log4j.Logger;

public class ComponentActivity extends
		AbstractAsynchronousActivity<ComponentActivityConfigurationBean>
		implements AsynchronousActivity<ComponentActivityConfigurationBean>,
		LockedNestedDataflow {
	private static final Logger logger = getLogger(ComponentActivity.class);
	private static final AnnotationTools aTools = new AnnotationTools();

	private volatile DataflowActivity componentRealization = new DataflowActivity();
	private ComponentActivityConfigurationBean configBean;
	
	private Dataflow realizingDataflow = null;

	@Override
	public void configure(ComponentActivityConfigurationBean configBean)
			throws ActivityConfigurationException {
		this.configBean = configBean;

		try {
			configurePorts(configBean.getPorts());
		} catch (RegistryException e) {
			throw new ActivityConfigurationException(
					"failed to get component realization", e);
		}

	}

	@Override
	public void executeAsynch(final Map<String, T2Reference> inputs,
			final AsynchronousActivityCallback callback) {

		try {
			ExceptionHandling exceptionHandling = configBean
					.getExceptionHandling();
			// InvocationContextImpl newContext =
			// copyInvocationContext(callback);

			AsynchronousActivityCallback useCallback = new ProxyCallback(
					callback, callback.getContext(), exceptionHandling);
			getComponentRealization().executeAsynch(inputs, useCallback);
		} catch (ActivityConfigurationException e) {
			callback.fail("Unable to execute component", e);
		}
	}

	@SuppressWarnings("unused")
	private InvocationContextImpl copyInvocationContext(
			final AsynchronousActivityCallback callback) {
		InvocationContext originalContext = callback.getContext();
		ReferenceService rs = originalContext.getReferenceService();
		InvocationContextImpl newContext = new InvocationContextImpl(rs, null);
		// for (Object o : originalContext.getEntities(Object.class)) {
		// newContext.addEntity(o);
		// }
		return newContext;
	}

	@Override
	public ComponentActivityConfigurationBean getConfiguration() {
		return configBean;
	}

	public DataflowActivity getComponentRealization()
			throws ActivityConfigurationException {
		synchronized (componentRealization) {
			if (componentRealization.getConfiguration() == null) {
//				try {
//					d = getDataflow(configBean);
//				} catch (RegistryException e) {
//					throw new ActivityConfigurationException(
//							"Unable to read dataflow", e);
//				}
				try {
					checkRealizingDataflow();
					componentRealization.configure(realizingDataflow);
				} catch (RegistryException e1) {
					logger.error("Unable to read dataflow", e1);
					throw new ActivityConfigurationException("Unable to read dataflow", e1);
				}

				for (Class<?> c : aTools.getAnnotatingClasses(this)) {
					String annotationValue = aTools.getAnnotationString(realizingDataflow, c,
							null);
					if (annotationValue == null)
						continue;
					try {
						aTools.setAnnotationString(this, c, annotationValue)
								.doEdit();
					} catch (EditException e) {
						logger.error("failed to set annotation string", e);
					}
				}

			}
			return componentRealization;
		}
	}

	private void checkRealizingDataflow() throws RegistryException {
		if (realizingDataflow == null) {
//			realizingDataflow = ComponentDataflowCache.getDataflow(getConfiguration());
			realizingDataflow = ComponentUtil.calculateComponentVersion(getConfiguration()).getDataflow();
		}		
	}
	
	@Override
	public Dataflow getNestedDataflow() {

		try {
			checkRealizingDataflow();
			return realizingDataflow;
//			return getDataflow(configBean);
		} catch (RegistryException e) {
			logger.error("failed to get component realization", e);
		}
		return null;
	}

}
