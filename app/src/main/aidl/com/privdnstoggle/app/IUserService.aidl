package com.privdnstoggle.app;

interface IUserService {
    // Reserved by the Shizuku server; transaction code 16777115.
    void destroy() = 16777114;

    // Runs "pm grant --user <userId> <packageName> <permission>" with shell (or root) privilege.
    // Returns an empty string on success, otherwise an error message.
    String grant(String packageName, String permission, int userId) = 1;
}
