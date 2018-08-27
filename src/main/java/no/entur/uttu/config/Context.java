package no.entur.uttu.config;

import com.google.common.base.Preconditions;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;


public class Context {

    private static ThreadLocal<Long> providerPerThread = new ThreadLocal<>();

    public static void setProvider(Long providerId) {
        Preconditions.checkArgument(providerId != null,
                "Attempt to set providerId = null for session", providerId);
        providerPerThread.set(providerId);
    }

    public static Long getProvider() {
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


    public static Long getVerifiedProviderId() {
        Long providerId = Context.getProvider();
        Preconditions.checkArgument(providerId != null,
                "Provider not set for session");
        return providerId;
    }

}
