package com.np2.bibliotecavirtual.validacao;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * O ano não pode ser maior que o ano atual.
 * <p>
 * Existe porque o {@code @Max} só aceita um número fixo, e o ano atual muda todo ano.
 * Valor nulo é aceito (quem obriga o campo é o {@code @NotNull}).
 */
@Documented
@Constraint(validatedBy = AnoNaoFuturoValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface AnoNaoFuturo {

    String message() default "O ano não pode ser maior que o ano atual";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
