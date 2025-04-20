package com.wiredi.runtime.security.authentication.authorities;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * An implementation of {@link Authority} that supports hierarchical relationships between authorities.
 *
 * <p>HierarchicalAuthority allows defining parent-child relationships between authorities,
 * where a parent authority implicitly grants all permissions of its child authorities.
 * This is useful for implementing role hierarchies where higher-level roles automatically
 * include the privileges of lower-level roles.</p>
 *
 * <p>For example, in a hierarchy where ADMIN &gt; MANAGER &gt; USER:</p>
 * <ul>
 *   <li>A user with ADMIN authority automatically has MANAGER and USER authorities</li>
 *   <li>A user with MANAGER authority automatically has USER authority</li>
 *   <li>When checking access, possessing ADMIN satisfies a requirement for MANAGER or USER</li>
 * </ul>
 *
 * <p>The hierarchy is maintained internally as a directed acyclic graph, allowing
 * complex role structures including multiple inheritance (a role can have multiple parents).</p>
 *
 * <p>Usage example:</p>
 * <pre>
 * // Create hierarchical authorities
 * HierarchicalAuthority adminRole = new HierarchicalAuthority("ADMIN");
 * HierarchicalAuthority managerRole = new HierarchicalAuthority("MANAGER");
 * HierarchicalAuthority userRole = new HierarchicalAuthority("USER");
 *
 * // Establish hierarchy: ADMIN > MANAGER > USER
 * adminRole.addChild(managerRole);
 * managerRole.addChild(userRole);
 *
 * // Check hierarchy
 * boolean hasAccess = adminRole.matches(userRole);  // Returns true
 * </pre>
 *
 * <p>Note that matching is asymmetric by design:</p>
 * <ul>
 *   <li>If a parent role authority matches a child role authority: true</li>
 *   <li>If a child role authority matches a parent role authority: false</li>
 * </ul>
 *
 * @see Authority
 * @see RoleAuthority
 */
public class HierarchicalAuthority implements Authority {

    private final String name;
    private final Set<HierarchicalAuthority> parents = new HashSet<>();
    private final Set<HierarchicalAuthority> children = new HashSet<>();

    /**
     * Creates a new hierarchical authority with the specified name.
     *
     * @param name the name of this authority (e.g., "ADMIN", "USER")
     */
    public HierarchicalAuthority(String name) {
        this.name = Objects.requireNonNull(name, "Authority name must not be null");
    }

    /**
     * Gets the name of this authority.
     *
     * @return the authority name
     */
    public String getName() {
        return name;
    }

    /**
     * Adds a child authority to this authority.
     * <p>
     * This establishes that this authority is a parent of the specified
     * child authority, meaning this authority inherits all the child's authorities.
     *
     * @param child the child authority to add
     * @return this authority for method chaining
     * @throws IllegalArgumentException if adding, the child would create a cycle in the hierarchy
     */
    public HierarchicalAuthority addChild(HierarchicalAuthority child) {
        if (child == this) {
            throw new IllegalArgumentException("Cannot add an authority as its own child");
        }

        // Check for cycles
        if (child.isAncestorOf(this)) {
            throw new IllegalArgumentException("Adding this child would create a cycle in the authority hierarchy");
        }

        children.add(child);
        child.parents.add(this);
        return this;
    }

    /**
     * Determines if this authority is an ancestor of the specified authority.
     * <p>
     * An authority is an ancestor if it's a parent, grandparent, etc. of another authority.
     *
     * @param other the authority to check
     * @return true if this authority is an ancestor of the other authority
     */
    public boolean isAncestorOf(HierarchicalAuthority other) {
        if (children.contains(other)) {
            return true;
        }

        for (HierarchicalAuthority child : children) {
            if (child.isAncestorOf(other)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Determines if this authority is a descendant of the specified authority.
     * <p>
     * An authority is a descendant if it is a child, grandchild, etc. of another authority.
     *
     * @param other the authority to check
     * @return true if this authority is a descendant of the other authority
     */
    public boolean isDescendantOf(HierarchicalAuthority other) {
        if (parents.contains(other)) {
            return true;
        }

        for (HierarchicalAuthority parent : parents) {
            if (parent.isDescendantOf(other)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Gets all direct child authorities of this authority.
     *
     * @return an unmodifiable set of child authorities
     */
    public Set<HierarchicalAuthority> getChildren() {
        return Collections.unmodifiableSet(children);
    }

    /**
     * Gets all direct parent authorities of this authority.
     *
     * @return an unmodifiable set of parent authorities
     */
    public Set<HierarchicalAuthority> getParents() {
        return Collections.unmodifiableSet(parents);
    }

    /**
     * Gets all authorities in the hierarchy below this authority.
     * <p>
     * This includes all direct children, grandchildren, and so on.
     *
     * @return a set of all descendant authorities
     */
    public Set<HierarchicalAuthority> getAllDescendants() {
        Set<HierarchicalAuthority> result = new HashSet<>();
        collectDescendants(result);
        return result;
    }

    private void collectDescendants(Set<HierarchicalAuthority> result) {
        for (HierarchicalAuthority child : children) {
            result.add(child);
            child.collectDescendants(result);
        }
    }

    /**
     * Determines if this hierarchical authority matches another authority.
     * <p>
     * This implementation matches:
     * <ul>
     *   <li>Another HierarchicalAuthority with the same name</li>
     *   <li>Any descendant HierarchicalAuthority in the hierarchy</li>
     *   <li>A RoleAuthority whose role matches this authority's name</li>
     * </ul>
     * <p>
     * The matching is asymmetric to respect the hierarchy. A parent matches its
     * children (you can access child resources with a parent role), but children
     * do not match their parents (you can't access parent resources with a child role).
     *
     * @param authority the authority to check against this authority
     * @return true if this authority matches the specified authority (i.e., grants at least
     * the same level of access), false otherwise
     */
    @Override
    public boolean matches(Authority authority) {
        if (authority == this) {
            return true;
        }

        if (authority instanceof HierarchicalAuthority other) {
            // Exact name match
            if (this.name.equals(other.name)) {
                return true;
            }

            // Check if the other authority is a descendant of this one
            return other.isDescendantOf(this);
        }

        if (authority instanceof RoleAuthority roleAuthority) {
            // Try to match a RoleAuthority by collecting all authority names in the hierarchy
            Set<HierarchicalAuthority> allDescendants = getAllDescendants();
            for (HierarchicalAuthority descendant : allDescendants) {
                RoleAuthority descendantRole = new RoleAuthority(descendant.getName());
                if (roleAuthority.matches(descendantRole)) {
                    return true;
                }
            }

            // Check if the role name directly matches this authority
            return new RoleAuthority(this.name).matches(roleAuthority);
        }

        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HierarchicalAuthority)) return false;
        HierarchicalAuthority that = (HierarchicalAuthority) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "HierarchicalAuthority[" + name + "]";
    }
}