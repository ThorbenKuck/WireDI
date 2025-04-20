package com.wiredi.runtime.security.authentication.authorities;

/**
 * Represents a security authority or permission within the authentication system.
 *
 * <p>An Authority defines a specific privilege, role, or permission that can be
 * granted to a user or authentication principal. Authorities are used to control
 * access to secured resources and operations through authorization checks.</p>
 *
 * <p>Implementations of this interface should define the specific semantics of what
 * constitutes a match between authorities. For example, hierarchical authorities
 * might consider an admin role to match a user role, while exact-match authorities
 * would only match authorities of the same type and value.</p>
 *
 * <p>Common implementations include:</p>
 * <ul>
 *   <li>Role-based authorities (e.g., "ADMIN", "USER")</li>
 *   <li>Permission-based authorities (e.g., "READ_DATA", "WRITE_DATA")</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>
 * // Check if a user has the required authority
 * boolean hasAccess = userAuthorities.stream()
 *     .anyMatch(userAuth -> userAuth.matches(requiredAuthority));
 * </pre>
 *
 * <p>When implementing custom Authority types, ensure that the {@link #matches(Authority)}
 * method correctly defines the matching logic appropriate for your security model.</p>
 *
 * @see SimpleAuthority
 * @see RoleAuthority
 * @see PermissionAuthority
 */
public interface Authority {

    /**
     * Determines if this authority matches another authority.
     *
     * <p>The exact semantics of what constitutes a "match" between authorities
     * is implementation-dependent and should be clearly documented by each
     * implementation. Some common matching strategies include:</p>
     *
     * <ul>
     *   <li><strong>Exact matching:</strong> Authorities match only if they are identical</li>
     *   <li><strong>Hierarchical matching:</strong> An authority matches if it represents
     *       a privilege level equal to or higher than the required authority</li>
     *   <li><strong>Pattern matching:</strong> An authority matches if it satisfies
     *       a pattern defined by the other authority</li>
     *   <li><strong>Attribute matching:</strong> Authorities match if certain attributes
     *       satisfy specific criteria</li>
     * </ul>
     *
     * <p>This method should be symmetric in most cases, meaning that if
     * {@code a.matches(b)} is true, then {@code b.matches(a)} should also be true.
     * However, some hierarchical authority implementations may have asymmetric
     * matching logic.</p>
     *
     * @param authority the authority to check against this authority
     * @return true if this authority matches the specified authority
     */
    boolean matches(Authority authority);

}
