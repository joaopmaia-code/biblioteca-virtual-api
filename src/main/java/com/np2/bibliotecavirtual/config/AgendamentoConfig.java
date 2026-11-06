package com.np2.bibliotecavirtual.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Liga as tarefas agendadas (@Scheduled), usadas pela RN05 (status ATRASADO).
 */
@Configuration
@EnableScheduling
public class AgendamentoConfig {
}
