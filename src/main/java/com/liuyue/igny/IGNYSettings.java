package com.liuyue.igny;


import carpet.api.settings.Rule;

import static com.liuyue.igny.utils.IGNYRuleCategory.*;


public class IGNYSettings
{
    @Rule(
            categories = {IGNY,SURVIVAL,FEATURE}
    )
    public static boolean wardenNeverDig = false;

    @Rule(
            categories = {IGNY,SURVIVAL,FEATURE}
    )
    public static boolean playerLevitationFreeShulkerBullet = false;

    @Rule(
            categories = {IGNY,SURVIVAL,FEATURE}
    )
    public static boolean playerMiningFatigueFreeGuardian = false;

    @Rule(
            categories = {IGNY,COMMAND,FEATURE},
            options = {"true", "false"}
    )
    public static boolean showRuleChangeHistory = false;

    @Rule(
            categories = {IGNY,SURVIVAL,FEATURE},
            options = {"true", "false"}
    )
    public static boolean fakePlayerCanPush = true;

    @Rule(
            categories = {IGNY,SURVIVAL,FEATURE},
            options = {"true", "false"}
    )
    public static Boolean wetSpongeCanAbsorbLava = false;

    @Rule(
            categories = {IGNY, COMMAND, FEATURE},
            options = {"false", "true", "ops", "0", "1", "2", "3", "4"}
    )
    public static String commandPlayerEnderChestDrop = "ops";

    @Rule(
            categories = {IGNY, SURVIVAL, FEATURE},
            options = {"false", "true"}
    )
    public static Boolean noWardenDarkness = false;

    @Rule(
            categories = {IGNY, SURVIVAL, FEATURE},
            options = {"false", "true"}
    )
    public static Boolean floatingIceWater = false;

    @Rule(
            categories = {IGNY, SURVIVAL, FEATURE},
            options = {"false", "true"}
    )
    public static Boolean noZombifiedPiglinNetherPortalSpawn = false;

    //#if MC >= 12102
    //$$ @Rule(
    //$$        categories = {IGNY,FEATURE},
    //$$        options = {"false", "true"}
    //$$ )
    //$$ public static Boolean projectileDuplicationReintroduced = false;
    //#endif

    //#if MC >= 12104
    //$$ @Rule(
    //$$        categories = {IGNY,FEATURE},
    //$$        options = {"false", "true"}
    //$$ )
    //$$ public static Boolean skeletonsPickupSwordsReintroduced = false;
    //#endif

    //#if MC >= 12102
    //$$ @Rule(
    //$$        categories = {IGNY,FEATURE},
    //$$        options = {"false", "true"}
    //$$ )
    //$$ public static Boolean minecartMotionFix = false;
    //#endif

    //#if MC < 12109
    @Rule(
            categories = {IGNY,FEATURE},
            options = {"false", "true"}
    )
    public static Boolean tntMinecartEmptyDamageSourceFix = false;
    //#endif

    @Rule(
            categories = {IGNY,FEATURE,BUGFIX},
            options = {"false", "true"}
    )
    public static Boolean fakePlayerBoatYawFix = false;

    @Rule(
            categories = {IGNY,FEATURE},
            options = {"false", "cantrade", "true"}
    )
    public static String killFakePlayerRemoveVehicle = "true";

    @Rule(
            categories = {IGNY,FEATURE},
            options = {"false", "true"}
    )
    public static Boolean candlePlaceOnIncompleteBlock = false;

    @Rule(
            categories = {IGNY, COMMAND, CREATIVE, FEATURE},
            options = {"false", "true", "ops", "0", "1", "2", "3", "4"}
    )
    public static String commandFixnotepitch = "ops";

    @Rule(
            categories = {IGNY, COMMAND, CREATIVE, FEATURE},
            options = {"false", "true"}
    )
    public static Boolean fixnotepitchUpdateBlock = false;

    //#if MC >= 12106
    //$$ @Rule(
    //$$        categories = {IGNY,SURVIVAL,FEATURE},
    //$$        options = {"false", "true"}
    //$$ )
    //$$ public static Boolean happyGhastNoClip = false;
    //#endif

    @Rule(
            categories = {IGNY, FEATURE},
            options = {"false", "true"}
    )
    public static Boolean onlyPlayerCreateNetherPortal = false;

    @Rule(
            categories = {IGNY, FEATURE},
            options = {"false", "true"}
    )
    public static Boolean noWitherEffect = false;

    @Rule(
            categories = {IGNY, COMMAND, FEATURE},
            options = {"false", "true", "ops", "0", "1", "2", "3", "4"}
    )
    public static String setDefaultArgument = "ops";

    //#if MC>=12105
    //$$ @Rule(
    //$$        categories = {IGNY, FEATURE},
    //$$        options = {"false", "true"}
    //$$ )
    //$$ public static Boolean zombifiedPiglinDropLootIfAngryReintroduced = false;
    //#endif

    //#if MC>=12106
    //$$ @Rule(
    //$$        categories = {IGNY, FEATURE},
    //$$        options = {"false", "true"}
    //$$ )
    //$$ public static Boolean locatorBarNoFakePlayer = false;
    //#endif

    @Rule(
            categories = {IGNY, COMMAND, FEATURE},
            options = {"false", "true"}
    )
    public static Boolean fakePlayerLoginLogoutNoChatInfo = false;
}
