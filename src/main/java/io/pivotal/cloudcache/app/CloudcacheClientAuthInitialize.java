package io.pivotal.cloudcache.app;

import java.io.Serializable;
import java.security.Principal;
import java.util.Properties;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.gemfire.config.annotation.support.AbstractAuthInitialize;
import org.springframework.data.gemfire.util.PropertiesBuilder;
import org.springframework.util.Assert;

import org.apache.geode.security.ResourcePermission;

public class CloudcacheClientAuthInitialize extends AbstractAuthInitialize {
  protected static final String SECURITY_PASSWORD_PROPERTY = "security-password";
  protected static final String SECURITY_USERNAME_PROPERTY = "security-username";
  protected static final String SECURITY_TOKEN_PROPERTY = "security-token";

  private final User user;

  /* (non-Javadoc) */
  public static CloudcacheClientAuthInitialize create() {
    return new CloudcacheClientAuthInitialize(User.newUser("some-username").with("some-password"));
  }

  /* (non-Javadoc) */
  public CloudcacheClientAuthInitialize(User user) {
    Assert.notNull(user, "User cannot be null");
    this.user = user;
  }

  /* (non-Javadoc) */
  @Override
  protected Properties doGetCredentials(Properties securityProperties) {
    return new PropertiesBuilder()
        .setProperty(SECURITY_USERNAME_PROPERTY, "")
        .setProperty(SECURITY_PASSWORD_PROPERTY, "")
        .setProperty(SECURITY_TOKEN_PROPERTY, "REPLACE ME")

//        good token for short-running test environment: PCC_ADMIN_511e78f3-05fc-47d1-b760-10592eab2446
//        .setProperty(SECURITY_TOKEN_PROPERTY, "eyJhbGciOiJSUzI1NiIsImprdSI6Imh0dHBzOi8vdWFhLnN5cy5wZXJzaWFub3JhbmdlLmNmLWFwcC5jb20vdG9rZW5fa2V5cyIsImtpZCI6ImtleS0xIiwidHlwIjoiSldUIn0.eyJqdGkiOiIxMTUxZjFmYTAxNTg0YjEwYmFlNjBmYjNkMGNlZDA0NiIsInN1YiI6Im15LWFwcC1jbGllbnQiLCJhdXRob3JpdGllcyI6WyJQQ0NfQURNSU5fNTExZTc4ZjMtMDVmYy00N2QxLWI3NjAtMTA1OTJlYWIyNDQ2Il0sInNjb3BlIjpbIlBDQ19BRE1JTl81MTFlNzhmMy0wNWZjLTQ3ZDEtYjc2MC0xMDU5MmVhYjI0NDYiXSwiY2xpZW50X2lkIjoibXktYXBwLWNsaWVudCIsImNpZCI6Im15LWFwcC1jbGllbnQiLCJhenAiOiJteS1hcHAtY2xpZW50IiwiZ3JhbnRfdHlwZSI6ImNsaWVudF9jcmVkZW50aWFscyIsInJldl9zaWciOiI2NGRmYThkNCIsImlhdCI6MTU4NTg0Nzc3NCwiZXhwIjoxNTg1ODkwOTc0LCJpc3MiOiJodHRwczovL3VhYS5zeXMucGVyc2lhbm9yYW5nZS5jZi1hcHAuY29tL29hdXRoL3Rva2VuIiwiemlkIjoidWFhIiwiYXVkIjpbIm15LWFwcC1jbGllbnQiXX0.i_csCpGGN2pFuH7UVAhGQcn43PxwaEe617GMIJK4eoJ49xVoK8tgQsH9icCPFu1anyMm3gkadXroGz9vEooGsOp_hN7FXFQGLL_j3AqvkH2eTSHxACRtHMzzdOGxjFbHVxiwD0nYZ9pJ5YmERutfE8bBiudflvDz9tDGjoFjTOIBxlhojopGYyZ3EeCi1RiwGfM11Xixd6WiLmnWHRjCFfe3NibKBdm2VR10yB1JEOepTEpNfLQ9m32xgk9UZnLMaGJTLht3oUTl4QsB0nPTUSmX_UMwVy0o7v04Qv_urKmbTMrp5q92ubIBVkwyzPv7h_-E8K-OXn4ju5aOpcALWw")

//        wrong space GUID: PCC_ADMIN_511e78f3-05fc-47d1-b760-10592eab2440
//        .setProperty(SECURITY_TOKEN_PROPERTY, "eyJhbGciOiJSUzI1NiIsImprdSI6Imh0dHBzOi8vdWFhLnN5cy5wZXJzaWFub3JhbmdlLmNmLWFwcC5jb20vdG9rZW5fa2V5cyIsImtpZCI6ImtleS0xIiwidHlwIjoiSldUIn0.eyJqdGkiOiJhOThkMGYyZjM4NWQ0NjhjYWEzZDQyODJhMjFjYjdlMyIsInN1YiI6Im15LWFwcC1jbGllbnQiLCJhdXRob3JpdGllcyI6WyJQQ0NfQURNSU5fNTExZTc4ZjMtMDVmYy00N2QxLWI3NjAtMTA1OTJlYWIyNDQwIl0sInNjb3BlIjpbIlBDQ19BRE1JTl81MTFlNzhmMy0wNWZjLTQ3ZDEtYjc2MC0xMDU5MmVhYjI0NDAiXSwiY2xpZW50X2lkIjoibXktYXBwLWNsaWVudCIsImNpZCI6Im15LWFwcC1jbGllbnQiLCJhenAiOiJteS1hcHAtY2xpZW50IiwiZ3JhbnRfdHlwZSI6ImNsaWVudF9jcmVkZW50aWFscyIsInJldl9zaWciOiJjOWRiNWM5ZCIsImlhdCI6MTU4NTg1NDQxMSwiZXhwIjoxNTg1ODk3NjExLCJpc3MiOiJodHRwczovL3VhYS5zeXMucGVyc2lhbm9yYW5nZS5jZi1hcHAuY29tL29hdXRoL3Rva2VuIiwiemlkIjoidWFhIiwiYXVkIjpbIm15LWFwcC1jbGllbnQiXX0.aZD9h6EDlFrAEmyzuQHQdT8tiqySNx4Ne2UCgRhhSg02RAvYhzHNjXnSsfYOwti-CBBAgrvEcx-IT3uUhA79vbVyAsJI0jyyZINkYeLucWJRAw_VukTf2ZXUNnPpgiPi5JW31XnsW-00VNZzjt267MBdK4xhyYMq6qVVCmdbBA-ua70lJsGicipgwacg8c6R7xYh0uJKbJMqn321VwQIZZ7TNs5iTdYj05o3qle_sIpMfyDw580G0S8ug70opbQY7yhbPUuHULqFeJq3WkMYNC8YzTwZOB25uCjzIroj5FIsLtljNl_ZbWuCbvOtNTAnefRPCfpamMXCdV6PLZxIZA")

//        wrong SCOPE: PCC_OTHER_ROLE_511e78f3-05fc-47d1-b760-10592eab2446
//        .setProperty(SECURITY_TOKEN_PROPERTY, "eeyJhbGciOiJSUzI1NiIsImprdSI6Imh0dHBzOi8vdWFhLnN5cy5wZXJzaWFub3JhbmdlLmNmLWFwcC5jb20vdG9rZW5fa2V5cyIsImtpZCI6ImtleS0xIiwidHlwIjoiSldUIn0.eyJqdGkiOiI3NGFiYmQ2MTgyNzQ0ZWEyYjhjMjNiYmYwMzM5NjUxZCIsInN1YiI6Im15LWFwcC1jbGllbnQiLCJhdXRob3JpdGllcyI6WyJQQ0NfT1RIRVJfUk9MRV81MTFlNzhmMy0wNWZjLTQ3ZDEtYjc2MC0xMDU5MmVhYjI0NDYiXSwic2NvcGUiOlsiUENDX09USEVSX1JPTEVfNTExZTc4ZjMtMDVmYy00N2QxLWI3NjAtMTA1OTJlYWIyNDQ2Il0sImNsaWVudF9pZCI6Im15LWFwcC1jbGllbnQiLCJjaWQiOiJteS1hcHAtY2xpZW50IiwiYXpwIjoibXktYXBwLWNsaWVudCIsImdyYW50X3R5cGUiOiJjbGllbnRfY3JlZGVudGlhbHMiLCJyZXZfc2lnIjoiYTY1YjI0Y2IiLCJpYXQiOjE1ODU4NTk3NzYsImV4cCI6MTU4NTkwMjk3NiwiaXNzIjoiaHR0cHM6Ly91YWEuc3lzLnBlcnNpYW5vcmFuZ2UuY2YtYXBwLmNvbS9vYXV0aC90b2tlbiIsInppZCI6InVhYSIsImF1ZCI6WyJteS1hcHAtY2xpZW50Il19.YSEg2roxbXudQNQE5JKiYRrahdXPQXAI-mXR5aringMg4O1vJuvvNAGJWj2rU191cH01gUf2wjKDg0d51JB6jAVbEEvdndtms7qhlnmqKLl04xb9-X6AZhULRhtPd1Tlb9tTokOL8peJZk0Cq2gMmi9gtINHwc8CUEhH4357NEBcQ40OoImOUXY5zXTRseFDVk7aBJgA0hOOJzs9LBuRDaQy2Gr-93Wna1gTpnJhLlaezFLbT3qYFWk1XId2iqbVMZXycIXPS4n0Kkj3pIUgHVH-d15FZUIHqjYdPXVRh_YoJw1ELC84CN5qQ2HUl4yuKhj6FXp_XpNuWvbAARbE7g")

//        signed by wrong private key
//        .setProperty(SECURITY_TOKEN_PROPERTY, "eyJhbGciOiJSUzI1NiIsImprdSI6Imh0dHBzOi8vdWFhLnN5cy5wZXJzaWFub3JhbmdlLmNmLWFwcC5jb20vdG9rZW5fa2V5cyIsImtpZCI6ImtleS0xIiwidHlwIjoiSldUIn0.eyJqdGkiOiIxMTUxZjFmYTAxNTg0YjEwYmFlNjBmYjNkMGNlZDA0NiIsInN1YiI6Im15LWFwcC1jbGllbnQiLCJhdXRob3JpdGllcyI6WyJQQ0NfQURNSU5fNTExZTc4ZjMtMDVmYy00N2QxLWI3NjAtMTA1OTJlYWIyNDQ2Il0sInNjb3BlIjpbIlBDQ19BRE1JTl81MTFlNzhmMy0wNWZjLTQ3ZDEtYjc2MC0xMDU5MmVhYjI0NDYiXSwiY2xpZW50X2lkIjoibXktYXBwLWNsaWVudCIsImNpZCI6Im15LWFwcC1jbGllbnQiLCJhenAiOiJteS1hcHAtY2xpZW50IiwiZ3JhbnRfdHlwZSI6ImNsaWVudF9jcmVkZW50aWFscyIsInJldl9zaWciOiI2NGRmYThkNCIsImlhdCI6MTU4NTg0Nzc3NCwiZXhwIjoxNTg1ODkwOTc0LCJpc3MiOiJodHRwczovL3VhYS5zeXMucGVyc2lhbm9yYW5nZS5jZi1hcHAuY29tL29hdXRoL3Rva2VuIiwiemlkIjoidWFhIiwiYXVkIjpbIm15LWFwcC1jbGllbnQiXX0.mCJAi6tY6BFWQ5Hob6oOSE6tryV331UvYtPjuRj_GqgdrDys_DrEFJdcA2SYZ9HSQUImoJ_8FCWmh351U_VnaaRxue-ZZQus5JkUS_3FYB25AVOukK3cIeMhPU4k8GjZVgHL3FxusLJO2oHrvXvRF5dw29dveQD6rrE1eeEA-1ZhwQ79Mrv8Di6MeGU7b3tu90hrAiTYuwPMwZnm4Ms-Oh5B9nUvGMvIQKzCITrIglWQcgFdHye1r8UHJ46XKHuuy8Df6mMr_Bh3fBFuo50y073JpwqiCkCmLUS5Nag2nHAjdtauDRmIbH5JAeFjp6h_MmAIZIm39ikqqPRLCOe9SWCO_QP-4qxMIvwqutMugeGRIYisv-AwjYxyPMz2DfaBNiV9XglXPNnPA6NdXjjZ2zvhJBMW5akVZbbsuVgzX6h2GYjdMnPUwFEyw9IDzzf9zlgW5f2ACUU-bzjkP6Cx8Pxak9RiH_22UFymojkBg9LL0EKfY_L-cG_maOD_-2uzozMXbll2V7O8-g4Y7zqFq_CQf1-nsIIqxzai0OQqxk-qWNov7jPad_CqeajgTsmDUiI6dhL3c2_VASah4Y48pWQxzEzDuYSuNNMFCXW1BCQwwZWcrk-K5Ma5wjh_HMlXB_ptGX0dQdo5neF_iGnZfIu4_Q0NDBG3N_a7okN9qGY")
        .build();
  }

  /* (non-Javadoc) */
  protected User getUser() {
    return this.user;
  }

  /**
   * @inheritDoc
   */
  @Override
  public String toString() {

    User user = getUser();

    return String.format("%1$s:%2$s", user.getName(), user.getCredentials());
  }

  @Getter
  @EqualsAndHashCode(of = { "name", "credentials" })
  public static class User implements /*Iterable<Role>,*/ Principal, Serializable {

    private String name;
    private String credentials;

    public String getCredentials() {
      return credentials;
    }

    public String getName() {
      return name;
    }

    public static User newUser(String name) {
      User user = new User();
      user.name = name;
      return user;
    }

    public boolean hasPermission(ResourcePermission permission) {
      return true;
    }

    public User with(String credentials) {

      this.credentials = credentials;

      return this;
    }
  }


}

