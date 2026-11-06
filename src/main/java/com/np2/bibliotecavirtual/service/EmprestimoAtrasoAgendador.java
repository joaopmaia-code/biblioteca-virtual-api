package com.np2.bibliotecavirtual.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * RN05: marca como ATRASADO os empréstimos com prazo vencido, ao subir a aplicação e a cada hora.
 * O agendamento é ligado pelo AgendamentoConfig (@EnableScheduling).
 */
@Component
@RequiredArgsConstructor
public class EmprestimoAtrasoAgendador {

    private final EmprestimoService emprestimoService;

    @EventListener(ApplicationReadyEvent.class)
    public void aoIniciar() {
        emprestimoService.atualizarAtrasados();
    }

    @Scheduled(cron = "0 0 * * * *")
    public void aCadaHora() {
        emprestimoService.atualizarAtrasados();
    }
}
