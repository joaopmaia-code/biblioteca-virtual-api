package com.np2.bibliotecavirtual.validacao;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Year;

/**
 * Regra da anotação {@link AnoNaoFuturo}: compara o valor com o ano atual no momento da requisição.
 */
public class AnoNaoFuturoValidator implements ConstraintValidator<AnoNaoFuturo, Integer> {

    @Override
    public boolean isValid(Integer ano, ConstraintValidatorContext context) {
        return ano == null || ano <= Year.now().getValue();
    }
}
