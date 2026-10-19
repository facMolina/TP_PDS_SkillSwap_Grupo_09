package ar.edu.uade.skillswap.infraestructura.soporte;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import ar.edu.uade.skillswap.infraestructura.persistencia.ConfiguracionBaseDatos;

public class RequiereBaseDatos implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext contexto) {
        String url = System.getenv(ConfiguracionBaseDatos.VARIABLE_URL);
        if (url == null || url.isBlank()) {
            return ConditionEvaluationResult.disabled(
                    "Requiere la variable de entorno " + ConfiguracionBaseDatos.VARIABLE_URL);
        }
        return ConditionEvaluationResult.enabled("Base de datos configurada");
    }
}
