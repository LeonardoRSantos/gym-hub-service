package br.com.gymhub.util;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CpfValidacao {

	public static boolean validar(String cpf) {

		if(cpf == null || cpf.isBlank()) {
			return false;
		}

		String cpfNormalizado = cpf.replaceAll("\\D", "");

		if(cpf.length() != 11) {
			return false;
		}

		if(cpf.matches("(\\d)\\1{10}")) {
			return false;
		}

		try {
			int soma = 0;

			for(int i = 0; i < 9; i ++) {
				soma += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
			}

			int primeiroDigito = 11 - (soma % 11);

			if(primeiroDigito >= 10) {
				primeiroDigito = 0;
			}

			if(primeiroDigito != Character.getNumericValue(cpf.charAt(9))) {
				return false;
			}

			soma = 0;

			for (int i = 0; i < 10; i++) {
				soma += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
			}

			int segundoDigito = 11 - (soma % 11);

			if (segundoDigito >= 10) {
				segundoDigito = 0;
			}

			return segundoDigito == Character.getNumericValue(cpf.charAt(10));
		} catch (Exception e) {
			return false;
		}
	}
}
