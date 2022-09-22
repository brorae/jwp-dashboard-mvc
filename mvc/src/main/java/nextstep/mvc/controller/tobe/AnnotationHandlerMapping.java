package nextstep.mvc.controller.tobe;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Set;
import nextstep.mvc.HandlerMapping;
import nextstep.web.annotation.Controller;
import nextstep.web.annotation.RequestMapping;
import nextstep.web.support.RequestMethod;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class AnnotationHandlerMapping implements HandlerMapping {

    private static final Logger log = LoggerFactory.getLogger(AnnotationHandlerMapping.class);

    private final Object[] basePackage;
    private final Map<HandlerKey, HandlerExecution> handlerExecutions;

    public AnnotationHandlerMapping(final Object... basePackage) {
        this.basePackage = basePackage;
        this.handlerExecutions = new HashMap<>();
    }

    public void initialize() {
        log.info("Initialized AnnotationHandlerMapping!");
        final Reflections reflections = new Reflections(basePackage);
        addHandlerExecutions(reflections);
    }

    private void addHandlerExecutions(final Reflections reflections) {
        final Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Controller.class);
        for (final Class<?> clazz : classes) {
            final Method[] methods = clazz.getDeclaredMethods();
            checkRequestMappingMethod(methods);
        }
    }

    private void checkRequestMappingMethod(final Method[] methods) {
        for (final Method method : methods) {
            if (method.isAnnotationPresent(RequestMapping.class)) {
                putHandlerExecution(method);
            }
        }
    }

    private void putHandlerExecution(final Method method) {
        final RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
        final RequestMethod[] applyingMethods = requestMapping.method();
        for (final RequestMethod applyingMethod : applyingMethods) {
            final HandlerKey handlerKey = new HandlerKey(requestMapping.value(), applyingMethod);
            final HandlerExecution handlerExecution = new HandlerExecution(method);
            handlerExecutions.put(handlerKey, handlerExecution);
        }
    }

    public Object getHandler(final HttpServletRequest request) {
        final HandlerKey handlerKey = new HandlerKey(request.getRequestURI(),
                RequestMethod.valueOf(request.getMethod()));
        try {
            return handlerExecutions.get(handlerKey);
        } catch (NullPointerException e) {
            log.error("Handler Not Found", e);
            throw new IllegalStateException();
        }
    }
}
