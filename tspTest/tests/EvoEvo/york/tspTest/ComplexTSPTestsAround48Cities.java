package EvoEvo.york.tspTest;


import EvoEvo.york.machineMetaModel.*;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.logging.Logger;

import static java.lang.Math.abs;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/** More complex TSP test with 48 cities in the dataset. Actually, it's the distances between the US state capitals of
 *  the lower 48 states.
 *
 *  The test data comes from https://people.sc.fsu.edu/~jburkardt/datasets/tsp/tsp.html, consulted on 1601201511 */
public class ComplexTSPTestsAround48Cities {
    private Logger _logger = Logger.getLogger("EvoEvo");
    private static final int _NUM_CITIES = 48;

    private Domain _reproducerDomain;
    private Domain _klonerDomain;
    private Domain _transcriberDomain;
    private Domain _translatorDomain;
    private CityType _cityDomain;

    @BeforeMethod
    public void setUp() throws Exception {
        _cityDomain = new CityType("TSP cities");

        for (int c = 1; c <= _NUM_CITIES; c++) {
            _cityDomain.addCity(String.valueOf(c));
        }

        // Add distances between cities:
        addCityDistances(1, _cityDomain.findCity(String.valueOf(1)), new int[]    {0, 4727, 1205, 6363, 3657, 3130, 2414, 563, 463, 5654, 1713, 1604, 2368, 2201, 1290, 1004, 3833, 2258, 3419, 2267, 2957, 720, 1700, 5279, 2578, 6076, 3465, 2654, 3625, 3115, 1574, 3951, 1748, 2142, 6755, 2383, 3306, 1029, 3530, 825, 2188, 4820, 3489, 1947, 6835, 1542, 2379, 3744});
        addCityDistances(2, _cityDomain.findCity(String.valueOf(2)), new int[]    {4727, 0, 3588, 2012, 1842, 6977, 6501, 5187, 5028, 2327, 4148, 4723, 3635, 3125, 4907, 3930, 7463, 6338, 7243, 5105, 4043, 4022, 3677, 2863, 3106, 1850, 7173, 6630, 1204, 6814, 6001, 3447, 5253, 2656, 3123, 6274, 7183, 5622, 3085, 4564, 2756, 1591, 7027, 6186, 3472, 5461, 4390, 2088});
        addCityDistances(3, _cityDomain.findCity(String.valueOf(3)), new int[]    {1205, 3588, 0, 5163, 2458, 3678, 3071, 1742, 1444, 4462, 1184, 1520, 1498, 1103, 1501, 951, 4298, 2903, 3967, 2169, 2209, 652, 828, 4136, 1518, 4873, 3954, 3254, 2446, 3581, 2441, 2960, 1966, 950, 5564, 2916, 3878, 2035, 2482, 1027, 1395, 3617, 3891, 2686, 5661, 2023, 1867, 2560});
        addCityDistances(4, _cityDomain.findCity(String.valueOf(4)), new int[]    {6363, 2012, 5163, 0, 2799, 8064, 7727, 6878, 6581, 1402, 5366, 5946, 4679, 4378, 6225, 5709, 8417, 7578, 8296, 6135, 4802, 5707, 4982, 2322, 4178, 320, 8186, 7800, 2778, 7859, 7408, 3763, 6461, 4223, 1427, 7451, 8263, 7131, 3669, 6011, 4638, 1681, 7987, 7502, 1877, 6758, 5360, 2844});
        addCityDistances(5, _cityDomain.findCity(String.valueOf(5)), new int[]    {3657, 1842, 2458, 2799, 0, 5330, 4946, 4200, 3824, 2012, 2573, 3157, 1924, 1580, 3427, 3179, 5749, 4793, 5577, 3409, 2223, 3066, 2185, 1860, 1401, 2491, 5486, 5035, 894, 5141, 4611, 1669, 3677, 1590, 3113, 4682, 5533, 4352, 1252, 3227, 2426, 1169, 5313, 4706, 3241, 3962, 2651, 304});
        addCityDistances(6, _cityDomain.findCity(String.valueOf(6)), new int[]    {3130, 6977, 3678, 8064, 5330, 0, 743, 3209, 2670, 6929, 2831, 2266, 3407, 3854, 2178, 4076, 727, 881, 293, 1930, 3310, 3672, 3315, 6199, 3932, 7745, 365, 482, 5774, 261, 1659, 4513, 1746, 4431, 7910, 769, 207, 2225, 4435, 2681, 5053, 6384, 550, 1224, 7805, 1670, 2704, 5230});
        addCityDistances(7, _cityDomain.findCity(String.valueOf(7)), new int[]    {2414, 6501, 3071, 7727, 4946, 743, 0, 2468, 1952, 6673, 2380, 1795, 3051, 3405, 1604, 3382, 1469, 168, 1020, 1681, 3110, 2993, 2827, 6009, 3552, 7412, 1104, 267, 5300, 821, 916, 4348, 1270, 3890, 7698, 332, 900, 1484, 4185, 2049, 4415, 6051, 1219, 482, 7635, 1054, 2432, 4884});
        addCityDistances(8, _cityDomain.findCity(String.valueOf(8)), new int[]    {563, 5187, 1742, 6878, 4200, 3209, 2468, 0, 718, 6203, 2241, 2051, 2920, 2762, 1687, 1304, 3932, 2331, 3487, 2669, 3487, 1175, 2260, 5840, 3141, 6596, 3563, 2728, 4120, 3240, 1559, 4507, 2082, 2658, 7304, 2512, 3364, 985, 4091, 1319, 2544, 5358, 3632, 1987, 7391, 1785, 2879, 4296});
        addCityDistances(9, _cityDomain.findCity(String.valueOf(9)), new int[]    {463, 5028, 1444, 6581, 3824, 2670, 1952, 718, 0, 5789, 1602, 1343, 2330, 2291, 970, 1451, 3376, 1796, 2959, 1951, 2835, 1112, 1725, 5346, 2628, 6285, 3007, 2193, 3889, 2661, 1122, 3920, 1372, 2391, 6883, 1927, 2845, 611, 3543, 676, 2590, 4993, 3039, 1486, 6934, 1112, 2196, 3876});
        addCityDistances(10, _cityDomain.findCity(String.valueOf(10)), new int[]    {5654, 2327, 4462, 1402, 2012, 6929, 6673, 6203, 5789, 0, 4392, 4947, 3648, 3501, 5274, 5183, 7216, 6535, 7140, 5022, 3621, 5077, 4090, 922, 3207, 1131, 7014, 6714, 2437, 6707, 6477, 2476, 5432, 3599, 1102, 6376, 7121, 6284, 2497, 5160, 4318, 937, 6795, 6507, 1268, 5773, 4249, 1914});
        addCityDistances(11, _cityDomain.findCity(String.valueOf(11)), new int[]    {1713, 4148, 1184, 5366, 2573, 2831, 2380, 2241, 1602, 4392, 0, 586, 766, 1029, 883, 2040, 3353, 2224, 3100, 1049, 1246, 1625, 503, 3841, 1196, 5054, 3042, 2488, 2945, 2676, 2087, 2331, 1114, 1650, 5459, 2132, 3037, 1958, 1997, 931, 2513, 3701, 2923, 2137, 5459, 1394, 711, 2534});
        addCityDistances(12, _cityDomain.findCity(String.valueOf(12)), new int[]    {1604, 4723, 1520, 5946, 3157, 2266, 1795, 2051, 1343, 4947, 586, 0, 1299, 1612, 406, 2208, 2824, 1639, 2542, 694, 1586, 1767, 1050, 4357, 1770, 5633, 2498, 1907, 3520, 2128, 1558, 2778, 531, 2171, 6003, 1552, 2472, 1538, 2506, 791, 2912, 4277, 2403, 1564, 5983, 827, 892, 3109});
        addCityDistances(13, _cityDomain.findCity(String.valueOf(13)), new int[]    {2368, 3635, 1498, 4679, 1924, 3407, 3051, 2920, 2330, 3648, 766, 1299, 0, 646, 1642, 2446, 3840, 2905, 3655, 1488, 730, 2096, 697, 3076, 533, 4363, 3567, 3122, 2453, 3219, 2842, 1592, 1791, 1480, 4706, 2772, 3610, 2721, 1232, 1656, 2550, 3001, 3403, 2860, 4697, 2126, 756, 1836});
        addCityDistances(14, _cityDomain.findCity(String.valueOf(14)), new int[]    {2201, 3125, 1103, 4378, 1580, 3854, 3405, 2762, 2291, 3501, 1029, 1612, 646, 0, 1853, 2026, 4349, 3247, 4119, 1997, 1341, 1753, 606, 3078, 419, 4070, 4052, 3517, 1923, 3690, 3032, 1866, 2142, 838, 4593, 3161, 4060, 2788, 1380, 1663, 1932, 2736, 3915, 3138, 4647, 2395, 1351, 1592});
        addCityDistances(15, _cityDomain.findCity(String.valueOf(15)), new int[]    {1290, 4907, 1501, 6225, 3427, 2178, 1604, 1687, 970, 5274, 883, 406, 1642, 1853, 0, 2029, 2803, 1438, 2466, 986, 1987, 1593, 1253, 4716, 2072, 5915, 2454, 1764, 3710, 2082, 1204, 3164, 497, 2287, 6342, 1419, 2379, 1134, 2867, 554, 2885, 4569, 2405, 1289, 6338, 555, 1297, 3406});
        addCityDistances(16, _cityDomain.findCity(String.valueOf(16)), new int[]    {1004, 3930, 951, 5709, 3179, 4076, 3382, 1304, 1451, 5183, 2040, 2208, 2446, 2026, 2029, 0, 4759, 3220, 4368, 2900, 3151, 442, 1765, 4960, 2444, 5443, 4396, 3610, 2932, 4034, 2572, 3891, 2525, 1590, 6278, 3313, 4261, 2033, 3398, 1476, 1241, 4287, 4390, 2928, 6419, 2428, 2749, 3337});
        addCityDistances(17, _cityDomain.findCity(String.valueOf(17)), new int[]    {3833, 7463, 4298, 8417, 5749, 727, 1469, 3932, 3376, 7216, 3353, 2824, 3840, 4349, 2803, 4759, 0, 1601, 477, 2359, 3617, 4345, 3851, 6433, 4372, 8098, 370, 1206, 6267, 726, 2384, 4754, 2335, 4991, 8148, 1452, 609, 2949, 4752, 3331, 5687, 6746, 437, 1948, 8005, 2334, 3098, 5618});
        addCityDistances(18, _cityDomain.findCity(String.valueOf(18)), new int[]    {2258, 6338, 2903, 7578, 4793, 881, 168, 2331, 1796, 6535, 2224, 1639, 2905, 3247, 1438, 3220, 1601, 0, 1165, 1563, 2988, 2829, 2666, 5882, 3401, 7263, 1233, 399, 5138, 923, 794, 4227, 1117, 3724, 7565, 286, 1049, 1348, 4051, 1881, 4248, 5903, 1322, 355, 7508, 887, 2302, 4736});
        addCityDistances(19, _cityDomain.findCity(String.valueOf(19)), new int[]    {3419, 7243, 3967, 8296, 5577, 293, 1020, 3487, 2959, 7140, 3100, 2542, 3655, 4119, 2466, 4368, 477, 1165, 0, 2170, 3520, 3965, 3588, 6393, 4183, 7977, 202, 767, 6041, 438, 1932, 4706, 2027, 4711, 8107, 1061, 132, 2503, 4652, 2972, 5344, 6617, 486, 1501, 7989, 1962, 2939, 5469});
        addCityDistances(20, _cityDomain.findCity(String.valueOf(20)), new int[]    {2267, 5105, 2169, 6135, 3409, 1930, 1681, 2669, 1951, 5022, 1049, 694, 1488, 1997, 986, 2900, 2359, 1563, 2170, 0, 1430, 2460, 1547, 4333, 2019, 5817, 2079, 1694, 3910, 1733, 1813, 2668, 654, 2694, 6029, 1366, 2130, 1991, 2525, 1474, 3542, 4455, 1923, 1641, 5957, 1071, 777, 3302});
        addCityDistances(21, _cityDomain.findCity(String.valueOf(21)), new int[]    {2957, 4043, 2209, 4802, 2223, 3310, 3110, 3487, 2835, 3621, 1246, 1586, 730, 1341, 1987, 3151, 3617, 2988, 3520, 1430, 0, 2779, 1387, 2905, 1062, 4482, 3398, 3119, 2922, 3087, 3115, 1240, 1953, 2175, 4607, 2796, 3501, 3119, 1136, 2173, 3268, 3136, 3189, 3029, 4527, 2355, 711, 2042});
        addCityDistances(22, _cityDomain.findCity(String.valueOf(22)), new int[]    {720, 4022, 652, 5707, 3066, 3672, 2993, 1175, 1112, 5077, 1625, 1767, 2096, 1753, 1593, 442, 4345, 2829, 3965, 2460, 2779, 0, 1401, 4781, 2166, 5427, 3984, 3212, 2946, 3620, 2224, 3603, 2089, 1496, 6178, 2906, 3861, 1719, 3132, 1040, 1479, 4211, 3969, 2553, 6290, 2012, 2336, 3189});
        addCityDistances(23, _cityDomain.findCity(String.valueOf(23)), new int[]    {1700, 3677, 828, 4982, 2185, 3315, 2827, 2260, 1725, 4090, 503, 1050, 697, 606, 1253, 1765, 3851, 2666, 3588, 1547, 1387, 1401, 0, 3621, 903, 4675, 3537, 2954, 2475, 3169, 2427, 2254, 1578, 1148, 5177, 2598, 3521, 2194, 1833, 1074, 2054, 3340, 3423, 2541, 5213, 1801, 1077, 2190});
        addCityDistances(24, _cityDomain.findCity(String.valueOf(24)), new int[]    {5279, 2863, 4136, 2322, 1860, 6199, 6009, 5840, 5346, 922, 3841, 4357, 3076, 3078, 4716, 4960, 6433, 5882, 6393, 4333, 2905, 4781, 3621, 0, 2718, 2042, 6254, 6024, 2569, 5966, 5913, 1687, 4807, 3384, 1716, 5699, 6384, 5787, 1852, 4687, 4285, 1272, 6022, 5892, 1629, 5178, 3581, 1639});
        addCityDistances(25, _cityDomain.findCity(String.valueOf(25)), new int[]    {2578, 3106, 1518, 4178, 1401, 3932, 3552, 3141, 2628, 3207, 1196, 1770, 533, 419, 2072, 2444, 4372, 3401, 4183, 2019, 1062, 2166, 903, 2718, 0, 3864, 4097, 3635, 1932, 3748, 3274, 1448, 2284, 1164, 4286, 3283, 4136, 3086, 967, 1973, 2285, 2507, 3935, 3331, 4312, 2589, 1284, 1340});
        addCityDistances(26, _cityDomain.findCity(String.valueOf(26)), new int[]    {6076, 1850, 4873, 320, 2491, 7745, 7412, 6596, 6285, 1131, 5054, 5633, 4363, 4070, 5915, 5443, 8098, 7263, 7977, 5817, 4482, 5427, 4675, 2042, 3864, 0, 7866, 7483, 2515, 7539, 7101, 3449, 6146, 3938, 1375, 7134, 7944, 6831, 3349, 5709, 4397, 1363, 7667, 7190, 1798, 6446, 5041, 2528});
        addCityDistances(27, _cityDomain.findCity(String.valueOf(27)), new int[]    {3465, 7173, 3954, 8186, 5486, 365, 1104, 3563, 3007, 7014, 3042, 2498, 3567, 4052, 2454, 4396, 370, 1233, 202, 2079, 3398, 3984, 3537, 6254, 4097, 7866, 0, 839, 5973, 374, 2019, 4569, 1996, 4669, 7970, 1085, 305, 2581, 4532, 2976, 5339, 6509, 287, 1581, 7844, 1974, 2838, 5369});
        addCityDistances(28, _cityDomain.findCity(String.valueOf(28)), new int[]    {2654, 6630, 3254, 7800, 5035, 482, 267, 2728, 2193, 6714, 2488, 1907, 3122, 3517, 1764, 3610, 1206, 399, 767, 1694, 3119, 3212, 2954, 6024, 3635, 7483, 839, 0, 5427, 558, 1181, 4349, 1377, 4044, 7723, 356, 653, 1744, 4218, 2241, 4614, 6121, 955, 743, 7644, 1231, 2465, 4957});
        addCityDistances(29, _cityDomain.findCity(String.valueOf(29)), new int[]    {3625, 1204, 2446, 2778, 894, 5774, 5300, 4120, 3889, 2437, 2945, 3520, 2453, 1923, 3710, 2932, 6267, 5138, 6041, 3910, 2922, 2946, 2475, 2569, 1932, 2515, 5973, 5427, 0, 5612, 4824, 2550, 4050, 1498, 3476, 5071, 5980, 4470, 2096, 3388, 1911, 1501, 5831, 4994, 3704, 4264, 3209, 1196});
        addCityDistances(30, _cityDomain.findCity(String.valueOf(30)), new int[]    {3115, 6814, 3581, 7859, 5141, 261, 821, 3240, 2661, 6707, 2676, 2128, 3219, 3690, 2082, 4034, 726, 923, 438, 1733, 3087, 3620, 3169, 5966, 3748, 7539, 374, 558, 5612, 0, 1716, 4280, 1624, 4298, 7679, 735, 420, 2263, 4216, 2606, 4967, 6179, 400, 1277, 7567, 1609, 2501, 5032});
        addCityDistances(31, _cityDomain.findCity(String.valueOf(31)), new int[]    {1574, 6001, 2441, 7408, 4611, 1659, 916, 1559, 1122, 6477, 2087, 1558, 2842, 3032, 1204, 2572, 2384, 794, 1932, 1813, 3115, 2224, 2427, 5913, 3274, 7101, 2019, 1181, 4824, 1716, 0, 4330, 1180, 3346, 7545, 1023, 1808, 578, 4062, 1438, 3693, 5763, 2115, 440, 7537, 763, 2404, 4603});
        addCityDistances(32, _cityDomain.findCity(String.valueOf(32)), new int[]    {3951, 3447, 2960, 3763, 1669, 4513, 4348, 4507, 3920, 2476, 2331, 2778, 1592, 1866, 3164, 3891, 4754, 4227, 4706, 2668, 1240, 3603, 2254, 1687, 1448, 3449, 4569, 4349, 2550, 4280, 4330, 0, 3184, 2510, 3402, 4031, 4698, 4281, 533, 3245, 3612, 2187, 4339, 4265, 3296, 3576, 1941, 1381});
        addCityDistances(33, _cityDomain.findCity(String.valueOf(33)), new int[]    {1748, 5253, 1966, 6461, 3677, 1746, 1270, 2082, 1372, 5432, 1114, 531, 1791, 2142, 497, 2525, 2335, 1117, 2027, 654, 1953, 2089, 1578, 4807, 2284, 6146, 1996, 1377, 4050, 1624, 1180, 3184, 0, 2685, 6475, 1022, 1952, 1341, 2963, 1050, 3358, 4787, 1926, 1086, 6436, 422, 1244, 3619});
        addCityDistances(34, _cityDomain.findCity(String.valueOf(34)), new int[]    {2142, 2656, 950, 4223, 1590, 4431, 3890, 2658, 2391, 3599, 1650, 2171, 1480, 838, 2287, 1590, 4991, 3724, 4711, 2694, 2175, 1496, 1148, 3384, 1164, 3938, 4669, 4044, 1498, 4298, 3346, 2510, 2685, 0, 4697, 3693, 4636, 2975, 1981, 1909, 1124, 2718, 4565, 3548, 4830, 2839, 2140, 1751});
        addCityDistances(35, _cityDomain.findCity(String.valueOf(35)), new int[]    {6755, 3123, 5564, 1427, 3113, 7910, 7698, 7304, 6883, 1102, 5459, 6003, 4706, 4593, 6342, 6278, 8148, 7565, 8107, 6029, 4607, 6178, 5177, 1716, 4286, 1375, 7970, 7723, 3476, 7679, 7545, 3402, 6475, 4697, 0, 7393, 8097, 7370, 3515, 6249, 5379, 2001, 7738, 7556, 461, 6829, 5267, 3013});
        addCityDistances(36, _cityDomain.findCity(String.valueOf(36)), new int[]    {2383, 6274, 2916, 7451, 4682, 769, 332, 2512, 1927, 6376, 2132, 1552, 2772, 3161, 1419, 3313, 1452, 286, 1061, 1366, 2796, 2906, 2598, 5699, 3283, 7134, 1085, 356, 5071, 735, 1023, 4031, 1022, 3693, 7393, 0, 965, 1542, 3883, 1913, 4286, 5772, 1121, 600, 7322, 902, 2128, 4608});
        addCityDistances(37, _cityDomain.findCity(String.valueOf(37)), new int[]    {3306, 7183, 3878, 8263, 5533, 207, 900, 3364, 2845, 7121, 3037, 2472, 3610, 4060, 2379, 4261, 609, 1049, 132, 2130, 3501, 3861, 3521, 6384, 4136, 7944, 305, 653, 5980, 420, 1808, 4698, 1952, 4636, 8097, 965, 0, 2380, 4629, 2877, 5250, 6583, 570, 1380, 7986, 1866, 2904, 5432});
        addCityDistances(38, _cityDomain.findCity(String.valueOf(38)), new int[]    {1029, 5622, 2035, 7131, 4352, 2225, 1484, 985, 611, 6284, 1958, 1538, 2721, 2788, 1134, 2033, 2949, 1348, 2503, 1991, 3119, 1719, 2194, 5787, 3086, 6831, 2581, 1744, 4470, 2263, 578, 4281, 1341, 2975, 7370, 1542, 2380, 0, 3952, 1127, 3197, 5518, 2658, 1002, 7395, 951, 2429, 4380});
        addCityDistances(39, _cityDomain.findCity(String.valueOf(39)), new int[]    {3530, 3085, 2482, 3669, 1252, 4435, 4185, 4091, 3543, 2497, 1997, 2506, 1232, 1380, 2867, 3398, 4752, 4051, 4652, 2525, 1136, 3132, 1833, 1852, 967, 3349, 4532, 4218, 2096, 4216, 4062, 533, 2963, 1981, 3515, 3883, 4629, 3952, 0, 2873, 3080, 2012, 4324, 4046, 3478, 3328, 1755, 1000});
        addCityDistances(40, _cityDomain.findCity(String.valueOf(40)), new int[]    {825, 4564, 1027, 6011, 3227, 2681, 2049, 1319, 676, 5160, 931, 791, 1656, 1663, 554, 1476, 3331, 1881, 2972, 1474, 2173, 1040, 1074, 4687, 1973, 5709, 2976, 2241, 3388, 2606, 1438, 3245, 1050, 1909, 6249, 1913, 2877, 1127, 2873, 0, 2374, 4392, 2943, 1659, 6285, 1012, 1563, 3254});
        addCityDistances(41, _cityDomain.findCity(String.valueOf(41)), new int[]    {2188, 2756, 1395, 4638, 2426, 5053, 4415, 2544, 2590, 4318, 2513, 2912, 2550, 1932, 2885, 1241, 5687, 4248, 5344, 3542, 3268, 1479, 2054, 4285, 2285, 4397, 5339, 4614, 1911, 4967, 3693, 3612, 3358, 1124, 5379, 4286, 5250, 3197, 3080, 2374, 0, 3386, 5284, 3997, 5585, 3386, 3125, 2664});
        addCityDistances(42, _cityDomain.findCity(String.valueOf(42)), new int[]    {4820, 1591, 3617, 1681, 1169, 6384, 6051, 5358, 4993, 937, 3701, 4277, 3001, 2736, 4569, 4287, 6746, 5903, 6617, 4455, 3136, 4211, 3340, 1272, 2507, 1363, 6509, 6121, 1501, 6179, 5763, 2187, 4787, 2718, 2001, 5772, 6583, 5518, 2012, 4392, 3386, 0, 6314, 5837, 2205, 5095, 3680, 1169});
        addCityDistances(43, _cityDomain.findCity(String.valueOf(43)), new int[]    {3489, 7027, 3891, 7987, 5313, 550, 1219, 3632, 3039, 6795, 2923, 2403, 3403, 3915, 2405, 4390, 437, 1322, 486, 1923, 3189, 3969, 3423, 6022, 3935, 7667, 287, 955, 5831, 400, 2115, 4339, 1926, 4565, 7738, 1121, 570, 2658, 4324, 2943, 5284, 6314, 0, 1676, 7603, 1964, 2662, 5184});
        addCityDistances(44, _cityDomain.findCity(String.valueOf(44)), new int[]    {1947, 6186, 2686, 7502, 4706, 1224, 482, 1987, 1486, 6507, 2137, 1564, 2860, 3138, 1289, 2928, 1948, 355, 1501, 1641, 3029, 2553, 2541, 5892, 3331, 7190, 1581, 743, 4994, 1277, 440, 4265, 1086, 3548, 7556, 600, 1380, 1002, 4046, 1659, 3997, 5837, 1676, 0, 7521, 744, 2325, 4670});
        addCityDistances(45, _cityDomain.findCity(String.valueOf(45)), new int[]    {6835, 3472, 5661, 1877, 3241, 7805, 7635, 7391, 6934, 1268, 5459, 5983, 4697, 4647, 6338, 6419, 8005, 7508, 7989, 5957, 4527, 6290, 5213, 1629, 4312, 1798, 7844, 7644, 3704, 7567, 7537, 3296, 6436, 4830, 461, 7322, 7986, 7395, 3478, 6285, 5585, 2205, 7603, 7521, 0, 6805, 5208, 3102});
        addCityDistances(46, _cityDomain.findCity(String.valueOf(46)), new int[]    {1542, 5461, 2023, 6758, 3962, 1670, 1054, 1785, 1112, 5773, 1394, 827, 2126, 2395, 555, 2428, 2334, 887, 1962, 1071, 2355, 2012, 1801, 5178, 2589, 6446, 1974, 1231, 4264, 1609, 763, 3576, 422, 2839, 6829, 902, 1866, 951, 3328, 1012, 3386, 5095, 1964, 744, 6805, 0, 1644, 3928});
        addCityDistances(47, _cityDomain.findCity(String.valueOf(47)), new int[]    {2379, 4390, 1867, 5360, 2651, 2704, 2432, 2879, 2196, 4249, 711, 892, 756, 1351, 1297, 2749, 3098, 2302, 2939, 777, 711, 2336, 1077, 3581, 1284, 5041, 2838, 2465, 3209, 2501, 2404, 1941, 1244, 2140, 5267, 2128, 2904, 2429, 1755, 1563, 3125, 3680, 2662, 2325, 5208, 1644, 0, 2532});
        addCityDistances(48, _cityDomain.findCity(String.valueOf(48)), new int[]    {3744, 2088, 2560, 2844, 304, 5230, 4884, 4296, 3876, 1914, 2534, 3109, 1836, 1592, 3406, 3337, 5618, 4736, 5469, 3302, 2042, 3189, 2190, 1639, 1340, 2528, 5369, 4957, 1196, 5032, 4603, 1381, 3619, 1751, 3013, 4608, 5432, 4380, 1000, 3254, 2664, 1169, 5184, 4670, 3102, 3928, 2532, 0});
        _cityDomain.setMutator((l, k) -> TestUtil.OptN(l, 3));

        _reproducerDomain = new Domain("Reproducer domain", Reproducer.class);
        _klonerDomain = new Domain("Kloner domain", Kloner.class);
        _transcriberDomain = new Domain("Transcriber domain", Transcriber.class);
        _translatorDomain = new Domain("Translater domain", Translator.class);

        Simulation.Initialise();
    }

    private void addCityDistances(int cityNum, City from, int[] distances) {
        for (int c = 1; c <=_NUM_CITIES; c++) {
            if (c != cityNum) {
                City to = _cityDomain.findCity(String.valueOf(c));
                _cityDomain.addCityDistance(from, to, distances[c-1]);
            }
        }
    }

    @AfterMethod
    public void tearDown() throws Exception {

    }

    @Test (priority = 1)
    public void testSimpleDistances1() throws Exception {
        City c1 = _cityDomain.findCity("1");
        City c15 = _cityDomain.findCity("15");


        assertEquals(_cityDomain.journeyTime(c1, c15), 1290.0);
    }


    @Test (priority = 1)
    public void testSimpleDistances2() throws Exception {
        City c22 = _cityDomain.findCity("22");
        City c47 = _cityDomain.findCity("47");


        assertEquals(_cityDomain.journeyTime(c22, c47), 2336.0);
    }


    @Test (priority = 1)
    public void ensureTripRoundAllCitiesIsTheSameDistanceRegardlessOfTheDirectionTaken() throws Exception {
        _cityDomain.setMutator((l, k) -> TestUtil.Reverse(l));

        // Create a new individual representing the route 1/2/3/...25/26/1
        List<Pearl> route = new ArrayList<>();
        for (int i = 1; i<=_NUM_CITIES; i++) {
            route.add(_cityDomain.findCity(String.valueOf(i)));
        }

        // Create a space that will contain the new individual and any constructed copes:
        TSPConcreteSpace world = new TSPConcreteSpace(Optional.empty());

        // Create a journey as an individual space within the world space:
        Journey j = new Journey(Optional.of(world));
        TestUtil.AddTTMachines(j, _transcriberDomain, _translatorDomain, _reproducerDomain, _klonerDomain, new ArrayList<>());

        // Create a structure that describes this route:
        Structure s = new Structure(j, route, _cityDomain);

        j.addMachineTemplate(s);

        // Check time of this journey:
        System.out.println(j);
        assertEquals(j.journeyTime(), 157553.0);

        // Replicate all the journeys in the world. meaning there should be two:
        world.replicate();
        assertEquals(world.numSubspaces(), 2);

        // Check that the new journey is the Reverse of the original:
        Journey copiedJourney = world.getAnotherSpace(j);
        assertFalse(j == copiedJourney);

        List<Pearl> newRoute = copiedJourney.locateMachine(TSPCalculator.class).getCode();
        assertEquals(route, TestUtil.Reverse(newRoute));

        // But it should still be the same time as the journey has just been reversed:
        assertEquals(copiedJourney.journeyTime(), j.journeyTime());
    }

    @Test (priority = 2)
    public void simpleIterationOfComplexRouteInBucketSpace() throws Exception {
        // Create a new individual representing the route 1/2/3/...25/26/1
        List<Pearl> route = new ArrayList<>();
//        int[] optimalRoute = new int[] {0, 1, 8, 38, 31, 44, 18, 7, 28, 6, 37, 19, 27, 17, 43, 30, 36, 46, 33,
//                                        20, 47, 21, 32, 39, 48, 5, 42, 24, 10, 45, 35, 4, 26, 2, 29, 34, 41, 16,
//                                        22, 3, 23, 14, 25, 13, 11, 12, 15, 40, 9};
        for (int i = 1; i<=_NUM_CITIES; i++) {
            route.add(_cityDomain.findCity(String.valueOf(i))); //String.valueOf(i))); //optimalRoute[i])));
        }

        // Create a space that will contain the new individual and any constructed copes:
        TSPConcreteSpace world = new TSPConcreteSpace(Optional.empty());

        // Make 10 identical journeys:
        for (int i = 0; i<2; i++) {
            makeJourney(route, world, _cityDomain);
        }

        assertEquals(world.numSubspaces(), 2);

        Journey best;

        int iteration = 0;
        do {
            best = generateOneNewJourney(world);
            iteration++;
        } while (iteration != 1000000 && best.journeyTime() > 35500);
        System.out.println("Iterations: " + iteration);

        assertTrue(best.journeyTime() < 35500.0);
    }

    private void makeJourney(List<Pearl> route, Space world, Domain domain) {
        Journey j = new Journey(Optional.of(world));
        TestUtil.AddTTMachines(j, _transcriberDomain, _translatorDomain, _reproducerDomain, _klonerDomain, new ArrayList<>());

        j.addMachineTemplate(new Structure(j, route, domain));
    }

    private Journey generateOneNewJourney(TSPConcreteSpace world) {
        Journey best = world.generate();
        System.out.println(best);
        return best;
    }


    @Test (priority = 20)
    public void searchInBucketUsingJavaStreamParellelisation() throws Exception {
        List<Pearl> route = TestUtil.CreateInitialRandomRoute(_cityDomain, _NUM_CITIES);

        // Create the world (a top level world) that will enact the individuals in a parallel manner:
        TSPConcreteSpace world = new TSPConcreteSpace(Optional.empty());

        // Add many instances of a journey over the already calculated route to the world:
        for (int i =0; i<100; i++) {
            this.makeJourney(route, world, _cityDomain);
            route = TestUtil.OptN(route, 10);
        }

        // Iterate many cycles of calculating all the journey times, chucking away the weakest half and
        // allowing the strongest half to reproduce:
        this.searchInSpace(world);
    }

    @Test (priority = 25)
    public void searchInBucketUsingJavaStreamParellelisationAndMutatingCopier() throws Exception {
        _cityDomain.setMutator((l, k) -> TestUtil.OptN(l, ((KlonerDomain)k.getDomain()).getDegree(k)));

        List<Pearl> route = TestUtil.CreateInitialRandomRoute(_cityDomain, _NUM_CITIES);
        TSPConcreteSpace world = new TSPConcreteSpace(Optional.empty());
        KlonerDomain kDomain = new KlonerDomain("Kloner domain", Kloner.class);

        // Add many instances of a journey over the already calculated route to the world:
        for (int i =0; i<Simulation.GetValue("numMGAJourneys", 100); i++) {
            TestUtil.MakeJourneyWithMutatingCopier(route, world, _cityDomain, kDomain, _transcriberDomain, _translatorDomain, _reproducerDomain);
        }

        // Iterate many cycles of calculating all the journey times, chucking away the weakest half and
        // allowing the strongest half to reproduce:
        this.searchInSpace(world);
    }

    @DataProvider (name = "kValues")
    public static Object[][] KValues() {
        return new Object[][] {{2, 10, 11}, {5, 10, 4}, {10, 2, 6}};
    }

    @Test (priority = 1, expectedExceptions = TSPTestException.class, dataProvider = "kValues")
    public void ensureExceptionForFoolishKValues(int mink, int maxk, int initialk) throws Exception {
        Simulation.SetValue("minKOpt", mink);
        Simulation.SetValue("maxKOpt", maxk);
        Simulation.SetValue("initialKOpt", initialk);

        // Create super space which will contain the locations in which individual journeys will "live"
        ToroidalTSP2DSpace world = new ToroidalTSP2DSpace(9, 9);

        // Create a journey in the centre of the new world using teh values just inserted into the Simulation object. This should
        // thrown an exception
        TestUtil.MakeJourneyInToroidalSpace(world, 4, 4, _NUM_CITIES, _cityDomain, _transcriberDomain, _translatorDomain, _reproducerDomain);

        assertFalse(true, "Should not get here");
    }

    @Test (priority = 30)
    public void searchInToroidalShapedBucketSpace() throws Exception {
        // Create super space which will contain the locations in which individual journeys will "live"
        ToroidalTSP2DSpace world = new ToroidalTSP2DSpace(9, 9);

        // Create a journey in the centre of the new world
        TestUtil.MakeJourneyInToroidalSpace(world, 4, 4, _NUM_CITIES, _cityDomain, _transcriberDomain, _translatorDomain, _reproducerDomain);

        assertEquals(world.numIndividuals(), 1, "New journey");
        this.searchInSpace(world);
    }

    private void searchInSpace(SearchableSpace world) {
        int i=0;
        Journey j;
        do {
            j = (Journey)world.search().get();
            if (i % 100 == 0)
                System.out.println(i + ": " + j);
            i++;
        } while (i != 1000000 && j.journeyTime() >= 35500);
        System.out.println(i + ": " + j);
    }

    @Test (priority = 40)
    public void multiThreadedSearchInEmptyToroidalBucketSpaceNeverDoesAnything() throws Exception {
        ToroidalTSP2DSpace world = new ToroidalTSP2DSpace(9, 9);

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(10);
        world.getSubspaces().forEach((s) -> executor.scheduleWithFixedDelay((Journey2DSite)s,
                                                                            0, 100, TimeUnit.MILLISECONDS));

        long time = System.currentTimeMillis();
        do {
        } while (!executor.isTerminated() && System.currentTimeMillis() < (time + 10000));
    }

    @Test (priority = 50)
    public void multiThreadedSearchInToroidalBucketSpaceWithSingleJourneyPopulatesSpace() throws Exception {
        ToroidalTSP2DSpace world = new ToroidalTSP2DSpace(9, 9);

        // Create a journey in the centre of the new world
        TestUtil.MakeJourneyInToroidalSpace(world, 4, 4, _NUM_CITIES, _cityDomain, _transcriberDomain, _translatorDomain, _reproducerDomain);

        assertEquals(world.numIndividuals(), 1, "New journey");

        // Create scheduled executor and give it all the sites in the world to look after:
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(10);
        world.getSubspaces().forEach((s) -> executor.scheduleWithFixedDelay((Journey2DSite)s,
                                                                            0, 10, TimeUnit.MILLISECONDS));
        executor.scheduleWithFixedDelay(world, 0, 100, TimeUnit.MICROSECONDS);

        long time = System.currentTimeMillis();
        do {
        } while (!executor.isTerminated() && System.currentTimeMillis() < (time + 30000));

        // After 10 seconds there should be lots of journeys in the world:
        System.out.println("There are now " + world.numIndividuals() + " journeys");
        assertTrue(world.numIndividuals() > 10, "Journey count");
    }

    @Test (priority = 100)
    public void multiThreadedSearchInToroidalSpaceWithSingleJourneyAsAStart() throws Exception {
        ToroidalTSP2DSpace world = new ToroidalTSP2DSpace(9, 9);

        // Create a journey in the centre of the new world
        TestUtil.MakeJourneyInToroidalSpace(world, 4, 4, _NUM_CITIES, _cityDomain, _transcriberDomain, _translatorDomain, _reproducerDomain);

        assertEquals(world.numIndividuals(), 1, "New journey");

        // Create scheduled executor and give it all the sites in the world to look after:
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(Simulation.GetValue("numThreads", 8));
        world.getSubspaces().forEach((s) -> executor.scheduleWithFixedDelay((Journey2DSite)s,
                                                                            0, 100, TimeUnit.MICROSECONDS));
        ScheduledFuture result = executor.scheduleWithFixedDelay(world, 0, 50, TimeUnit.MICROSECONDS);

        Optional<Individual> best;
        long time = System.currentTimeMillis();
        do {
            Thread.sleep(500);
            best = world.best();
            if (best.isPresent()) {
                System.out.printf("Best of %d is: %s%n",
                                  world.numIndividuals(),
                                  best.get().getContainer().get());
            } else {
                System.out.printf("Best of %d is: nobody%n",
                                  world.numIndividuals());
            }
        } while ((best.isPresent() ? ((Journey)(best.get())).journeyTime() : 1000000) > 35500 &&
                 System.currentTimeMillis() < (time + 60000) &&
                 !result.isDone());

        if (result.isDone()) {
            try {
                Object r = result.get();
                System.out.println("result is " + r);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        executor.shutdownNow();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {}

        Machine.FlushLogger();
        _logger.fine(String.format("{%d} Completed, best is %s", System.currentTimeMillis(), best.get().getContainer().get()));
        Machine.FlushLogger();

        assertTrue(world.numIndividuals() > 10, "Journey count");
        System.out.println("Best: " + best);
    }

    @Test (priority = 100)
    public void multiThreadedSearchInToroidalSpaceWithMultipleJourneysAsAStart() throws Exception {
        ToroidalTSP2DSpace world = new ToroidalTSP2DSpace(Simulation.GetValue("xSize", 9), Simulation.GetValue("ySize", 9));

        // Make a number of journeys in the world:
        for (int i = 0; i < Simulation.GetValue("numToroidalJourneys", 20); i++) {
            int[] position = world.findEmptySpace();
            TestUtil.MakeJourneyInToroidalSpace(world, position[0], position[1], _NUM_CITIES,
                                                _cityDomain, _transcriberDomain, _translatorDomain, _reproducerDomain);
        }

        assertEquals(world.numIndividuals(),
                     Simulation.GetValue("numToroidalJourneys"),
                     "New journey count");

        // Create scheduled executor and give it all the sites in the world to look after:
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(Simulation.GetValue("numThreads", 8));
        world.getSubspaces().forEach((s) -> executor.scheduleWithFixedDelay((Journey2DSite)s,
                                                                            0, 100, TimeUnit.MICROSECONDS));
        ScheduledFuture result = executor.scheduleWithFixedDelay(world, 0, 50, TimeUnit.MICROSECONDS);

        Optional<Individual> best;
        long time = System.currentTimeMillis();
        do {
            Thread.sleep(500);
            best = world.best();
            if (best.isPresent()) {
                System.out.printf("Best of %d is: %s%n",
                                  world.numIndividuals(),
                                  best.get().getContainer().get());
            } else {
                System.out.printf("Best of %d is: nobody%n",
                                  world.numIndividuals());
            }
        } while ((best.isPresent() ? ((Journey)(best.get())).journeyTime() : 1000000) > 35500 &&
                 System.currentTimeMillis() < (time + 60000) &&
                 !result.isDone());

        if (result.isDone()) {
            try {
                Object r = result.get();
                System.out.println("result is " + r);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        executor.shutdownNow();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {}

        Machine.FlushLogger();
        _logger.fine(String.format("{%d} Completed, best is %s", System.currentTimeMillis(), best.get().getContainer().get()));
        Machine.FlushLogger();

        assertTrue(world.numIndividuals() > 10, "Journey count");
        System.out.println("Best: " + best);
    }

    @Test (priority = 100)
    public void singleThreadedSearchInBucketSpaceUsingMicrobialGA() throws Exception {
        // Create SearchableSpace for MGA approach:
        SearchableSpace world = new MicrobialGATSPSpace();

        // Create a complete set of journeys in the new space:
        int numJourneys = Simulation.GetValue("numMGAJourneys", 100);
        KlonerDomain kDomain = new KlonerDomain("Kloner domain", Kloner.class);
        for (int i = 0; i < numJourneys; i++) {
            List<Pearl> route = TestUtil.CreateInitialRandomRoute(_cityDomain, _NUM_CITIES);
            TestUtil.MakeJourneyWithMutatingCopier(route, (Space)world, _cityDomain, kDomain, _transcriberDomain, _translatorDomain, _reproducerDomain);
        }

        assertEquals(world.numIndividuals(), numJourneys, "Number of journeys in MicrobialGA");

        Journey best;
        long time = System.currentTimeMillis();
        int searchCount = 0;
        do {
                best = (Journey)world.search().get();
                if (searchCount++ % 100 == 0) {
                    System.out.printf("Best of %d is: %s%n", world.numIndividuals(), best);
                }
        } while (best.journeyTime() > Simulation.GetValue("targetTime", 35500)
                 &&
                 System.currentTimeMillis() < (time + Simulation.GetValue("totalRunTimeInMilliseconds", 60000)));

        Machine.FlushLogger();
        _logger.fine(String.format("{%d} Completed, best is %s", System.currentTimeMillis(), best));
        Machine.FlushLogger();

        assertEquals(world.numIndividuals(), numJourneys, "Number of journeys in MicrobialGA at end");
        assertTrue(best.journeyTime() < Simulation.GetValue("microbialGASuccessThresholdTime", 50000), "MicrobialGA threshold time");
        System.out.println("Best: " + best);
    }
}