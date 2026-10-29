package br.com.gymhub.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class CpfValidacaoTest {

    @ParameterizedTest
    @ValueSource(strings = {"52998224725", "11144477735", "12345678909"})
    @DisplayName("CPFs com digitos verificadores corretos devem ser aceitos")
    void dadoCpfValidoDeveAceitar(String cpf) {
        assertThat(CpfValidacao.validar(cpf)).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "123", "529982247250", "52998224724", "52998224715", "abcdefghijk",
            "00000000000", "11111111111", "22222222222", "33333333333", "44444444444",
            "55555555555", "66666666666", "77777777777", "88888888888", "99999999999"})
    @DisplayName("CPFs ausentes, repetidos ou com digitos incorretos devem ser rejeitados")
    void dadoCpfInvalidoDeveRejeitar(String cpf) {
        assertThat(CpfValidacao.validar(cpf)).isFalse();
    }
}
