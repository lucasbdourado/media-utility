/**
 * Persistence boundary for operation metadata and events.
 *
 * <p>Operation metadata is persisted through JPA. Operation events are
 * implemented by a later dedicated task. Media bytes must stay out of the
 * database.</p>
 */
package com.lucasdourado.mediautility.persistence;
