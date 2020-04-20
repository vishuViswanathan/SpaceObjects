KPL/PCK

  Mass parameters for planets & satellites used in Horizons

  Note: 
    1) Parameter "BODY000_GMLIST" contains the list of all objects 
        with a defined GM, in ascending SPK ID code order. 
    2) Masses USED FOR DYNAMICS in Horizons asteroid/comet numerical 
       integrations are ...
         1-2,4-10, 301, 399
         2000001, 2000002, 2000003, 2000004, 2000010, 2000015, 
         2000016, 2000031, 2000048, 2000052, 2000065, 2000087,
         2000088, 2000451, 2000511, 2000704
        ... where 1-9 are planetary system barycenters, 10 is the Sun,
            301 is the Moon, 399 is the Earth, and 2xxxxxx are selected
            large asteroid perturbers
    3) Any other masses shown (including asteroid Eros, 2000433) are used only  
        when computing osculating elements for targets involved with them
    4) Horizons only needs to use the product GM, not G by itself.

  Sources: DE-431 "ASTRO-VALUES", Folkner              [1-10,199,299,301,399] 
           Jacobson satellite file release forms [non-Lunar satellites, planets]
           SB431-N16 small-body integration perturber file (DE431 masses)

  Units: km^3/s^2

  Modification history:

   DATE         Who  Change
   -----------  ---  -------------------------------------------------------
   2000-Nov-28  JDG  Version 1.0
   2002-Oct-17  JDG  C-P-V values made current
   2003-Feb-26  JDG  Pluto/Charon values consistent w/Jacobson PLU006
   2003-Mar-13  JDG  Update all satellite/planet GMs
   2005-Mar-02  JDG  Update Saturnians 601-609,699 to SAT192 values
   2005-Mar-07  JDG  Update Pluto system (901,999) to PLU009 values
   2005-Mar-18  JDG  Update 610-611, add 615-617 (SAT196)
   2006-Apr-28  JDG  Update 601-609, 699 (SAT242) and 401-402, 499 (MAR063)
   2006-Sep-28  JDG  Update 601-609, 699 (SAT252)
   2008-Aug-11  JDG  Revert to DE405 for Pluto system GM (9)
   2008-Sep-05  JDG  Over-ride DE405 "4" with 499+401+402.
   2008-Sep-25  JDG  Update 4,499,401,402 for MAR080.
   2013-Jul-23  JDG  Version 2.0
                      Updated planets to DE431 values (from DE405)
                      401-402,499    : MAR097
                      501-505,599    : JUP230
                      601-609,699    : SAT359
                      610-611,615-617: SAT357
                      701-705,799    : URA083 
                      801,899        : NEP081
                      901-904,999    : PLU042 (902-904 newly added)
                      2000001-2000004: BIG16 (smb perturber file value)
                      2000006-2000007: BIG16
                      2000010        : BIG16
                      2000015-2000016: BIG16
                      2000029        : BIG16
                      2000052        : BIG16
                      2000065        : BIG16
                      2000087-2000088: BIG16
                      2000433        : Yeomans et al. 
                                       (2000) Science v.289,pp.2085-2088
                      2000511        : BIG16
                      2000704        : BIG16
   2013-Dec-30  JDG  Updated for SAT360, PLU043
   2014-Jan-08  JDG  Updated for URA111/112.
   2015-Nov-05  JDG  Updated for PLU055 (901-904, 905 added, 999)
   2016-Apr-05  JDG  Updated for SAT382 (610-611,615-617,699: 2015 update)
   2016-Jun-17  JDG  Updated for SAT389.14 (601-609,612 added, 699)
                     Updated for JUP310 (501-505, 599)
   2016-Sep-13  JDG  Updated for JUP340 (506 added, 599)
   2016-Oct-11  JDG  Updated for SAT393
   2017-May-09  JDG  Version 3.0
                      Updated to DE431/N16 perturber model (Horizons 4.05)
                       Removed:  2000006, 2000007, 2000029
                       Added  :  2000031, 2000048, 2000451
                       Masses updated for consistency with DE430/431: 
                        2000001: DE431/N16 
                        2000002: DE431/N16 
                        2000003: DE431/N16 
                        2000004: DE431/N16 
                        2000010: DE431/N16 
                        2000015: DE431/N16 
                        2000016: DE431/N16 
                        2000052: DE431/N16 
                        2000065: DE431/N16 
                        2000087: DE431/N16 
                        2000088: DE431/N16 
                        2000511: DE431/N16
                        2000704: DE431/N16
  Key added:
   JDG= Jon.D.Giorgini@jpl.nasa.gov

   \begindata

     BODY000_GMLIST= ( 1 2 3 4 5 6 7 8 9 10
                     199 299 
                     301 399 
                     401 402 499 
                     501 502 503 504 505 506 599
                     601 602 603 604 605 606 607 608 609 610 611 612 615 616 699
                     701 702 703 704 705 799
                     801 899
                     901 902 903 904 905 999
                     2000001 2000002 2000003 2000004 2000010 2000015 2000016
                     2000031 2000048 2000052 2000065 2000087 2000088 2000433
                     2000451 2000511 2000704 )

     BODY1_GM       = ( 2.2031780000000021E+04 )
     BODY2_GM       = ( 3.2485859200000006E+05 )
     BODY3_GM       = ( 4.0350323550225981E+05 )
     BODY4_GM       = ( 4.2828375214000022E+04 )
     BODY5_GM       = ( 1.2671276480000021E+08 )
     BODY6_GM       = ( 3.7940585200000003E+07 )
     BODY7_GM       = ( 5.7945486000000080E+06 )
     BODY8_GM       = ( 6.8365271005800236E+06 )
     BODY9_GM       = ( 9.7700000000000068E+02 )
     BODY10_GM      = ( 1.3271244004193938E+11 )

     BODY199_GM     = ( 2.2031780000000021E+04 )
     BODY299_GM     = ( 3.2485859200000006E+05 )
     BODY399_GM     = ( 3.9860043543609598E+05 )
     BODY499_GM     = ( 4.282837362069909E+04  )
     BODY599_GM     = ( 1.266865349164126E+08  )
     BODY699_GM     = ( 3.793120723493890E+07  )
     BODY799_GM     = ( 5.793951322279009E+06  )
     BODY899_GM     = ( 6.835099502439672E+06  )
     BODY999_GM     = ( 8.693390780381926E+02  )
 
     BODY301_GM     = ( 4.9028000661637961E+03 )

     BODY401_GM     = ( 7.087546066894452E-04 )
     BODY402_GM     = ( 9.615569648120313E-05 )

     BODY501_GM     = ( 5.959924010272514E+03 )
     BODY502_GM     = ( 3.202739815114734E+03 )
     BODY503_GM     = ( 9.887819980080976E+03 )
     BODY504_GM     = ( 7.179304867611079E+03 )
     BODY505_GM     = ( 1.487604677404272E-01 )
     BODY506_GM     = ( 1.331862640548012E-01 )
 
     BODY601_GM     = ( 2.503458199931431E+00 )
     BODY602_GM     = ( 7.211185066509890E+00 )
     BODY603_GM     = ( 4.120856508658532E+01 )
     BODY604_GM     = ( 7.311574218947423E+01 )
     BODY605_GM     = ( 1.539419035933117E+02 )
     BODY606_GM     = ( 8.978137030983542E+03 )
     BODY607_GM     = ( 3.712085754472412E-01 )
     BODY608_GM     = ( 1.205095752388872E+02 )
     BODY609_GM     = ( 5.532371285376407E-01 )
     BODY610_GM     = ( 1.265765099012197E-01 )
     BODY611_GM     = ( 3.512333288208074E-02 )
     BODY612_GM     = ( 3.424829447502984E-04 )
     BODY615_GM     = ( 3.718871247516475E-04 )
     BODY616_GM     = ( 1.075208001007610E-02 )
     BODY617_GM     = ( 9.290325122028795E-03 )

     BODY701_GM     = ( 8.346344431770477E+01 )
     BODY702_GM     = ( 8.509338094489388E+01 )
     BODY703_GM     = ( 2.269437003741248E+02 )
     BODY704_GM     = ( 2.053234302535623E+02 )
     BODY705_GM     = ( 4.319516899232100E+00 )

     BODY801_GM     = ( 1.427598140725034E+03 )
  
     BODY901_GM     = ( 1.062509269522026E+02 )
     BODY902_GM     = ( 2.150552267969335E-03 )
     BODY903_GM     = ( 3.663917107480563E-03 )
     BODY904_GM     = ( 4.540734312735987E-04 )
     BODY905_GM     = ( 2.000000000000000E-20 )

     BODY2000001_GM = ( 6.2809393000000000E+01 )
     BODY2000002_GM = ( 1.3923011000000001E+01 )
     BODY2000003_GM = ( 1.6224149999999999E+00 )
     BODY2000004_GM = ( 1.7288008999999999E+01 )
     BODY2000010_GM = ( 5.5423920000000004E+00 )
     BODY2000015_GM = ( 2.0981550000000002E+00 )
     BODY2000016_GM = ( 1.5300480000000001E+00 )
     BODY2000031_GM = ( 2.8448720000000001E+00 )
     BODY2000048_GM = ( 1.1351590000000000E+00 )
     BODY2000052_GM = ( 1.1108039999999999E+00 )
     BODY2000065_GM = ( 1.4264810000000001E+00 )
     BODY2000087_GM = ( 9.8635300000000004E-01 )
     BODY2000088_GM = ( 1.1557990000000000E+00 )
     BODY2000433_GM = ( 4.463E-4 )
     BODY2000451_GM = ( 1.0295259999999999E+00 )
     BODY2000511_GM = ( 2.3312860000000000E+00 )
     BODY2000704_GM = ( 2.3573170000000001E+00 )

   \begintext

