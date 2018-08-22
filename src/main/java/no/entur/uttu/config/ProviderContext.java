package no.entur.uttu.config;

import com.google.common.base.Preconditions;

public class ProviderContext {

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

}
