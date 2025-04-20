package com.wiredi.runtime.security.authentication.authorities;

/**
 * An implementation of {@link Authority} that represents a fine-grained permission-based authority.
 *
 * <p>Permission authorities provide more granular access control than role-based authorities,
 * allowing for specific actions or resources to be protected. While roles typically represent
 * a collection of permissions assigned to a user, permissions represent the specific privileges
 * or actions that can be performed.</p>
 *
 * <p>Common permission naming patterns include:</p>
 * <ul>
 *   <li><strong>Action-based</strong>: "READ", "WRITE", "DELETE", "EXECUTE"</li>
 *   <li><strong>Resource-based</strong>: "USER_MANAGEMENT", "REPORT_ACCESS", "SYSTEM_CONFIG"</li>
 *   <li><strong>Combined</strong>: "READ_USER", "MODIFY_REPORT", "VIEW_LOGS"</li>
 *   <li><strong>Hierarchical</strong>: "reports.create", "reports.view.confidential"</li>
 * </ul>
 *
 * <p>This implementation uses exact string matching to determine if authorities match.
 * Two PermissionAuthority instances match only if they represent exactly the same permission.</p>
 *
 * <p>Usage example:</p>
 * <pre>
 * // Create permission authorities
 * Authority createPermission = new PermissionAuthority("CREATE_USER");
 * Authority readPermission = new PermissionAuthority("READ_USER");
 *
 * // Check if a user has the required permission
 * boolean canCreate = userPermissions.stream()
 *     .anyMatch(permission -> permission.matches(createPermission));
 *
 * // In authorization checks
 * if (securityContext.hasPermission(new PermissionAuthority("MODIFY_REPORT"))) {
 *     // Allow the user to modify reports
 * }
 * </pre>
 *
 * <p>Permission authorities are often used in conjunction with role-based authorities, where
 * roles contain collections of permissions. Authorization systems may check both role and
 * permission authorities when determining access.</p>
 *
 * @see Authority
 * @see RoleAuthority
 * @see HierarchicalAuthority
 */
public class PermissionAuthority implements Authority {

    /**
     * The permission identifier this authority represents.
     */
    private final String permission;

    /**
     * Creates a new PermissionAuthority with the specified permission identifier.
     *
     * @param permission the identifier of the permission (e.g., "READ_DATA", "CREATE_USER")
     */
    public PermissionAuthority(String permission) {
        this.permission = permission;
    }

    /**
     * Determines if this permission authority matches another authority.
     *
     * <p>This implementation only matches other PermissionAuthority instances
     * with exactly the same permission identifier. The match is case-sensitive.</p>
     *
     * <p>For example:</p>
     * <ul>
     *   <li>PermissionAuthority("READ_DATA") matches PermissionAuthority("READ_DATA")</li>
     *   <li>PermissionAuthority("READ_DATA") does not match PermissionAuthority("read_data")</li>
     *   <li>PermissionAuthority("READ_DATA") does not match RoleAuthority("ADMIN") or other non-PermissionAuthority implementations</li>
     * </ul>
     *
     * @param authority the authority to check against this permission authority
     * @return true if the provided authority is a PermissionAuthority with the same permission identifier,
     *         false otherwise
     */
    @Override
    public boolean matches(Authority authority) {
        if (authority instanceof PermissionAuthority permissionAuthority) {
            return permissionAuthority.permission.equals(permission);
        }

        return false;
    }
}
