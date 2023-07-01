package br.com.aspenmc.bukkit.permission.regex;

/**
 * Este codigo nao pertence ao autor do plugin.
 * Este codigo pertence ao criador do PermissionEX
 */

import br.com.aspenmc.bukkit.BukkitCommon;
import br.com.aspenmc.bukkit.permission.FieldReplacer;
import br.com.aspenmc.bukkit.permission.PermissionCheckResult;
import br.com.aspenmc.bukkit.permission.PermissionMatcher;
import br.com.aspenmc.bukkit.permission.RegExpMatcher;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.bukkit.permissions.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("rawtypes")
public class PermissiblePEX extends PermissibleBase {

    private static final FieldReplacer<PermissibleBase, Map> PERMISSIONS_FIELD = new FieldReplacer<>(
            PermissibleBase.class, "permissions", Map.class);
    private static final FieldReplacer<PermissibleBase, List> ATTACHMENTS_FIELD = new FieldReplacer<>(
            PermissibleBase.class, "attachments", List.class);
    private static final Method CALC_CHILD_PERMS_METH;

    static {
        try {
            CALC_CHILD_PERMS_METH = PermissibleBase.class.getDeclaredMethod("calculateChildPermissions", Map.class,
                                                                            boolean.class, PermissionAttachment.class);
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
        CALC_CHILD_PERMS_METH.setAccessible(true);
    }

    private final Map<String, PermissionAttachmentInfo> permissions;
    private final List<PermissionAttachment> attachments;
    private static final AtomicBoolean LAST_CALL_ERRORED = new AtomicBoolean(false);

    protected final Player player;
    private Permissible previousPermissible = null;
    protected final Map<String, PermissionCheckResult> cache = new ConcurrentHashMap<>();
    private final Object permissionsLock = new Object();

    private final PermissionMatcher matcher = new RegExpMatcher();

    public PermissiblePEX(Player player) {
        super(player);
        this.player = player;
        permissions = new LinkedHashMap<String, PermissionAttachmentInfo>() {

            private static final long serialVersionUID = 1L;

            @Override
            public PermissionAttachmentInfo put(String k, PermissionAttachmentInfo v) {
                PermissionAttachmentInfo existing = this.get(k);
                if (existing != null) {
                    return existing;
                }
                return super.put(k, v);
            }
        };
        PERMISSIONS_FIELD.set(this, permissions);
        this.attachments = ATTACHMENTS_FIELD.get(this);
        recalculatePermissions();
    }

    public Permissible getPreviousPermissible() {
        return previousPermissible;
    }

    public void setPreviousPermissible(Permissible previousPermissible) {
        this.previousPermissible = previousPermissible;
    }

    @Override
    public boolean hasPermission(String permission) {
        PermissionCheckResult res = permissionValue(permission);
        switch (res) {
        case TRUE:
        case FALSE:
            return res.toBoolean();
        case UNDEFINED:
        default:
            if (super.isPermissionSet(permission)) {
                return super.hasPermission(permission);
            } else {
                Permission perm = player.getServer().getPluginManager().getPermission(permission);
                return perm == null ? Permission.DEFAULT_PERMISSION.getValue(player.isOp()) :
                       perm.getDefault().getValue(player.isOp());
            }
        }
    }

    @Override
    public boolean hasPermission(Permission permission) {
        PermissionCheckResult res = permissionValue(permission.getName());
        switch (res) {
        case TRUE:
        case FALSE:
            return res.toBoolean();
        case UNDEFINED:
        default:
            if (super.isPermissionSet(permission.getName())) {
                return super.hasPermission(permission);
            } else {
                return permission.getDefault().getValue(player.isOp());
            }
        }
    }

    @Override
    public void recalculatePermissions() {
        if (permissions != null && attachments != null) {
            synchronized (permissionsLock) {
                clearPermissions();
                cache.clear();
                for (ListIterator<PermissionAttachment> it = this.attachments.listIterator(this.attachments.size());
                     it.hasPrevious(); ) {
                    PermissionAttachment attach = it.previous();
                    calculateChildPerms(attach.getPermissions(), attach);
                }
                for (Permission p : player.getServer().getPluginManager().getDefaultPermissions(isOp())) {
                    this.permissions.put(p.getName(), new PermissionAttachmentInfo(player, p.getName(), null, true));
                    calculateChildPerms(p.getChildren(), null);
                }
            }
        }
    }

    protected void calculateChildPerms(Map<String, Boolean> children, PermissionAttachment attachment) {
        try {
            CALC_CHILD_PERMS_METH.invoke(this, children, false, attachment);
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isPermissionSet(String permission) {
        return super.isPermissionSet(permission) || permissionValue(permission) != PermissionCheckResult.UNDEFINED;
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        synchronized (permissionsLock) {
            return new LinkedHashSet<>(permissions.values());
        }
    }

    private PermissionCheckResult checkSingle(String expression, String permission, boolean value) {
        if (matcher.isMatches(expression, permission)) {
            return PermissionCheckResult.fromBoolean(value);
        }
        return PermissionCheckResult.UNDEFINED;
    }

    protected PermissionCheckResult permissionValue(String permission) {
        try {
            Validate.notNull(permission, "Permissions being checked must not be null!");
            permission = permission.toLowerCase();
            PermissionCheckResult res = cache.get(permission);
            if (res != null) {
                return res;
            }

            res = PermissionCheckResult.UNDEFINED;

            synchronized (permissionsLock) {
                for (PermissionAttachmentInfo pai : permissions.values()) {
                    if ((res = checkSingle(pai.getPermission(), permission, pai.getValue())) !=
                        PermissionCheckResult.UNDEFINED) {
                        break;
                    }
                }
            }
            if (res == PermissionCheckResult.UNDEFINED) {
                for (Map.Entry<String, Boolean> ent : BukkitCommon.getInstance().getRegexPerms().getPermissionList()
                                                                  .getParents(permission)) {
                    if ((res = permissionValue(ent.getKey())) != PermissionCheckResult.UNDEFINED) {
                        res = PermissionCheckResult.fromBoolean(res.toBoolean() == ent.getValue());
                        break;
                    }
                }
            }
            cache.put(permission, res);
            LAST_CALL_ERRORED.set(false);
            return res;
        } catch (Throwable t) {
            if (LAST_CALL_ERRORED.compareAndSet(false, true)) {
                t.printStackTrace();
            }
            return PermissionCheckResult.UNDEFINED;
        }
    }
}