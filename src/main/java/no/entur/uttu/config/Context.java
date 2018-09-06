package no.entur.uttu.config;

import com.google.common.base.Preconditions;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;


public class Context {

    private static ThreadLocal<String> providerPerThread = new ThreadLocal<>();

    public static void setProvider(String providerCode) {
        Preconditions.checkArgument(providerCode != null,
                "Attempt to set providerCode = null for session");
        providerPerThread.set(providerCode);
    }

    public static String getProvider() {
        return providerPerThread.get();
    }

    public static void clear() {
        providerPerThread.remove();
    }

    public static String getUsername() {
        String user = "unknown";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            user = Objects.toString(auth.getPrincipal());
        }
        return user;
    }


    public static String getVerifiedProviderCode() {
        String providerCode = Context.getProvider();
        Preconditions.checkArgument(providerCode != null,
                "Provider not set for session");
        return providerCode;
    }

}
