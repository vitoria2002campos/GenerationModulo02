package com.generation.service;

import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.generation.model.Usuario;
import com.generation.model.UsuarioLogin;
import com.generation.repository.UsuarioRepository;

@Service
public class UsuarioService {

	@Autowired
	private UsuarioRepository usuarioRepository;

	private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

	/**
	 * Método para criptografar a senha
	 */
	public String encoder(String senha) {

		return encoder.encode(senha);
	}

	/**
	 * Método para listar os todos os usuários
	 */
	public List<Usuario> listarUsuarios(){
		
		return usuarioRepository.findAll();
		
	}
	
	/**
	 * Método para buscar um usuário pelo Id
	 */
	public Optional <Usuario> buscarUsuarioId(long id) {
		
		return usuarioRepository.findById(id);
		
	}
	
	public Optional<Usuario> cadastrarUsuario(Usuario usuario) {

		if (usuarioRepository.findByUsuario(usuario.getUsuario()).isPresent())
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O Usuário já existe!", null);

		/**
		 * Clacula a idade do usuário e verifica se ele é maior de idade
		 */
		int idade = Period.between(usuario.getDataNascimento(), LocalDate.now()).getYears();

		if (idade < 18)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O Usuário é menor de idade!", null);

		usuario.setSenha(encoder(usuario.getSenha()));

		return Optional.of(usuarioRepository.save(usuario));

	}

	public Optional<Usuario> atualizarUsuario(Usuario usuario) {

		if (usuarioRepository.findByUsuario(usuario.getUsuario()).isPresent()) {
			
			int idade = Period.between(usuario.getDataNascimento(), LocalDate.now()).getYears();
	
			if (idade < 18)
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O Usuário é menor de idade!", null);
	
			usuario.setSenha(encoder(usuario.getSenha()));
	
			return Optional.of(usuarioRepository.save(usuario));
		
		}else {
			
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "O Usuário não encontrado!", null);
			
		}

	}
	
	public Optional <UsuarioLogin> loginUsuario(Optional <UsuarioLogin> usuarioLogin){
		
		Optional <Usuario> usuario = usuarioRepository.findByUsuario(usuarioLogin.get().getUsuario());
		
		if (usuario.isPresent()) {
			
			if(encoder.matches(usuarioLogin.get().getSenha(), usuario.get().getSenha())) {
				
				String auth = usuarioLogin.get().getUsuario() + ":" + usuarioLogin.get().getSenha();
				byte[] encodeAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
				String authHeader = "Basic " + new String(encodeAuth);
				
				usuarioLogin.get().setId(usuario.get().getId());
				usuarioLogin.get().setNome(usuario.get().getNome());
				usuarioLogin.get().setSenha(usuario.get().getSenha());
				usuarioLogin.get().setToken(authHeader);
				
				return usuarioLogin;
			}
		}
	
		/**
	 	* Força o Lançamento de uma Exception (Erro de excessão)
	 	*/
		throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "O Usuário ou Senha Inválidos!", null);
	}
}

