package com.football.KickBoard.common.security;

import java.util.Collection;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

  private final Object principal;
  private Object credentials;

  public JwtAuthenticationToken(Object principal,Object credentials){
    super(null);
    this.principal = principal;
    this.credentials = credentials;
    setAuthenticated(false);
  }
  public JwtAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities){
  super(authorities);
  this.principal = principal;
  this.credentials = credentials;
  setAuthenticated(true);
  }

  @Override
  public Object getPrincipal() {
    return principal;
  }

  @Override
  public Object getCredentials() {
    return credentials;
  }
}
