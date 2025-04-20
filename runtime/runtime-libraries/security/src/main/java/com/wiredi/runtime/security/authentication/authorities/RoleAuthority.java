package com.wiredi.runtime.security.authentication.authorities;

/**
 * An implementation of {@link Authority} that represents a role-based security authority.
 *
 * <p>Role-based authorities are one of the most common forms of authorization
 * in security systems. Each RoleAuthority instance represents a single role that
 * can be assigned to users or principals, such as "ADMIN", "USER", "MANAGER", etc.</p>
 *
 * <p>This implementation uses exact string matching to determine if authorities match.
 * Two RoleAuthority instances match only if they represent exactly the same role.</p>
 *
 * <p>Examples of common roles:</p>
 * <ul>
 *   <li>ADMIN - Typically represents administrative access with full privileges</li>
 *   <li>USER - Basic user access with limited privileges</li>
 *   <li>MANAGER - Middle-tier access typically with some administrative capabilities</li>
 *   <li>GUEST - Limited, read-only access</li>
 * </ul>
 *
 * <p>Usage example:</p>
 * <pre>
 * // Create role authorities
 * Authority adminRole = new RoleAuthority("ADMIN");
 * Authority userRole = new RoleAuthority("USER");
 *
 * // Check if a required role matches a user's role
 * boolean hasAccess = userRole.matches(requiredRole);
 *
 * // Add to a user's authorities collection
 * List&lt;Authority&gt; userAuthorities = new ArrayList&lt;&gt;();
 * userAuthorities.add(new RoleAuthority("USER"));
 * userAuthorities.add(new RoleAuthority("REPORT_VIEWER"));
 * </pre>
 *
 * @see Authority
 * @see SimpleAuthority
 */
public class RoleAuthority implements Authority{

    /**
     * The role name this authority represents.
     */
    private final String role;

    /**
     * Creates a new RoleAuthority with the specified role name.
     *
     * @param role the name of the role (e.g., "ADMIN", "USER")
     */
    public RoleAuthority(String role) {
        this.role = role;
    }

    /**
     * Determines if this role authority matches another authority.
     *
     * <p>This implementation only matches other RoleAuthority instances
     * with exactly the same role name. The match is case-sensitive.</p>
     *
     * <p>For example:</p>
     * <ul>
     *   <li>RoleAuthority("ADMIN") matches RoleAuthority("ADMIN")</li>
     *   <li>RoleAuthority("ADMIN") does not match RoleAuthority("admin")</li>
     *   <li>RoleAuthority("ADMIN") does not match any non-RoleAuthority implementation</li>
     * </ul>
     *
     * @param authority the authority to check against this role authority
     * @return true if the provided authority is a RoleAuthority with the same role,
     *         false otherwise
     */
    @Override
    public boolean matches(Authority authority) {
        if (authority instanceof RoleAuthority roleAuthority) {
            return roleAuthority.role.equals(role);
        }

        return false;
    }
}
