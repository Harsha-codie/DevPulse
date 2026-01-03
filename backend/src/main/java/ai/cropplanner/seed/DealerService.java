package ai.cropplanner.seed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DealerService {
    
    private static final Logger logger = LoggerFactory.getLogger(DealerService.class);
    
    // Dummy dealer database
    private final List<Dealer> dealers = new ArrayList<>();
    
    public DealerService() {
        initializeDealers();
    }
    
    private void initializeDealers() {
        // BANGALORE dealers  
        Dealer d1 = new Dealer(1L, "Sri Manjunatha Seeds & Fertilisers", "Yeshwanthpur", "BANGALORE",
            "9845012345", "Yeshwanthpur, Bangalore",
            new String[]{"Cabbage - Golden Acre", "Tomato - Abhinav", "Carrot - Nantes", "Beans - Arka Komal"});
        d1.setSeedPrices(Map.of("Cabbage", "₹450/kg", "Tomato", "₹800/kg", "Carrot", "₹350/kg", "Beans", "₹280/kg"));
        dealers.add(d1);
        
        Dealer d2 = new Dealer(2L, "Bhoomi Krishi Kendra", "Malleswaram", "BANGALORE",
            "9880123456", "Malleswaram, Bangalore",
            new String[]{"Rice - BPT 5204", "Maize - Kaveri 50", "Chickpea - Kabuli", "Mustard - Varuna"});
        d2.setSeedPrices(Map.of("Rice", "₹120/kg", "Maize", "₹200/kg", "Chickpea", "₹180/kg", "Mustard", "₹150/kg"));
        dealers.add(d2);
        
        Dealer d3 = new Dealer(3L, "Nandini Agro Seeds", "JP Nagar", "BANGALORE",
            "9449876543", "JP Nagar, Bangalore",
            new String[]{"Tomato - Arya", "Chilli - G4", "Brinjal - Bhagyamati", "Cauliflower - Snowball"});
        d3.setSeedPrices(Map.of("Tomato", "₹750/kg", "Chilli", "₹600/kg", "Brinjal", "₹420/kg", "Cauliflower", "₹380/kg"));
        dealers.add(d3);
        
        // MYSORE dealers
        Dealer d4 = new Dealer(4L, "Kaveri Seed House", "Vijayanagar", "MYSORE",
            "9686543210", "Vijayanagar, Mysore",
            new String[]{"Cabbage - Pride of India", "Carrot - Kuroda", "Beetroot - Detroit Dark"});
        d4.setSeedPrices(Map.of("Cabbage", "₹420/kg", "Carrot", "₹330/kg", "Beetroot", "₹290/kg"));
        dealers.add(d4);
        
        Dealer d5 = new Dealer(5L, "Basava Krishi Stores", "Kuvempunagar", "MYSORE",
            "9741234567", "Kuvempunagar, Mysore",
            new String[]{"Rice - Jyothi", "Ragi - GPU 28", "Maize - Super 900M"});
        d5.setSeedPrices(Map.of("Rice", "₹110/kg", "Ragi", "₹95/kg", "Maize", "₹190/kg"));
        dealers.add(d5);
        
        // MANGALORE dealers
        Dealer d6 = new Dealer(6L, "Coastal Krishi Seva", "Balmatta", "MANGALORE",
            "9482223344", "Balmatta, Mangalore",
            new String[]{"Rice - Jaya", "Coconut - Tall", "Banana - Robusta", "Cashew - BPP-8"});
        d6.setSeedPrices(Map.of("Rice", "₹105/kg", "Coconut", "₹45/nut", "Banana", "₹90/sucker", "Cashew", "₹850/kg"));
        dealers.add(d6);
        
        Dealer d7 = new Dealer(7L, "Mangala Seed Corporation", "Bejai", "MANGALORE",
            "9632112233", "Bejai, Mangalore",
            new String[]{"Pepper - Panniyur-1", "Ginger - Rio-de-Janeiro", "Turmeric - Lakadong"});
        d7.setSeedPrices(Map.of("Pepper", "₹750/kg", "Ginger", "₹220/kg", "Turmeric", "₹180/kg"));
        dealers.add(d7);
        
        // HUBLI dealers
        Dealer d8 = new Dealer(8L, "Renuka Seeds Centre", "Gokul Road", "HUBLI",
            "9900887766", "Gokul Road, Hubli",
            new String[]{"Tomato - Abhinav F1", "Chilli - Arka Lohit", "Onion - Arka Kalyan"});
        d8.setSeedPrices(Map.of("Tomato", "₹780/kg", "Chilli", "₹580/kg", "Onion", "₹320/kg"));
        dealers.add(d8);
        
        Dealer d9 = new Dealer(9L, "Lakshmi Agro Agency", "Vidyanagar", "HUBLI",
            "9845554433", "Vidyanagar, Hubli",
            new String[]{"Cabbage - Green Express", "Cauliflower - Pusa Snowball", "Beetroot - Crimson"});
        d9.setSeedPrices(Map.of("Cabbage", "₹440/kg", "Cauliflower", "₹390/kg", "Beetroot", "₹300/kg"));
        dealers.add(d9);
        
        // BELGAUM dealers
        Dealer d10 = new Dealer(10L, "Vigneshwara Agro Traders", "Hindwadi", "BELGAUM",
            "9448765432", "Hindwadi, Belgaum",
            new String[]{"Sugarcane - CO 86032", "Cotton - Bt Bunny", "Maize - NAH 1137"});
        d10.setSeedPrices(Map.of("Sugarcane", "₹85/kg", "Cotton", "₹650/pkt", "Maize", "₹210/kg"));
        dealers.add(d10);
        
        Dealer d11 = new Dealer(11L, "GreenField Seeds & Inputs", "Camp Area", "BELGAUM",
            "9731098765", "Camp Area, Belgaum",
            new String[]{"Wheat - HD 2967", "Chickpea - JG 11", "Groundnut - TMV 2"});
        d11.setSeedPrices(Map.of("Wheat", "₹65/kg", "Chickpea", "₹170/kg", "Groundnut", "₹140/kg"));
        dealers.add(d11);
        
        // TUMKUR dealers
        Dealer d12 = new Dealer(12L, "Sai Seeds & Pesticides", "BH Road", "TUMKUR",
            "9343556677", "BH Road, Tumkur",
            new String[]{"Coconut - Tall x Dwarf", "Ragi - ML 365", "Groundnut - TAG 24"});
        d12.setSeedPrices(Map.of("Coconut", "₹48/nut", "Ragi", "₹100/kg", "Groundnut", "₹135/kg"));
        dealers.add(d12);
        
        // UTTARA KANNADA dealers
        Dealer d13 = new Dealer(13L, "Coastal Agro Seeds", "Karwar", "UTTARA KANNADA",
            "9448223344", "Karwar, Uttara Kannada",
            new String[]{"Arecanut - Mangala", "Cashew - Vengurla-4", "Pepper - Panniyur-5", "Coconut - WCT"});
        d13.setSeedPrices(Map.of("Arecanut", "₹350/kg", "Cashew", "₹900/kg", "Pepper", "₹780/kg", "Coconut", "₹50/nut"));
        dealers.add(d13);
        
        Dealer d14 = new Dealer(14L, "Sirsi Krishi Kendra", "Sirsi", "UTTARA KANNADA",
            "9886112233", "Sirsi, Uttara Kannada",
            new String[]{"Paddy - Jyothi", "Ragi - HR 911", "Maize - Kaveri 288", "Groundnut - GPBD 4"});
        d14.setSeedPrices(Map.of("Paddy", "₹115/kg", "Ragi", "₹98/kg", "Maize", "₹205/kg", "Groundnut", "₹145/kg"));
        dealers.add(d14);
        
        Dealer d15 = new Dealer(15L, "Kumta Seed House", "Kumta", "UTTARA KANNADA",
            "9741998877", "Kumta, Uttara Kannada",
            new String[]{"Rice - MTU 1010", "Banana - Grand Naine", "Turmeric - Rajendra Sonia", "Ginger - Suprabha"});
        d15.setSeedPrices(Map.of("Rice", "₹108/kg", "Banana", "₹95/sucker", "Turmeric", "₹190/kg", "Ginger", "₹230/kg"));
        dealers.add(d15);
        
        // DAKSHINA KANNADA dealers
        Dealer d16 = new Dealer(16L, "Puttur Agro Centre", "Puttur", "DAKSHINA KANNADA",
            "9632445566", "Puttur, Dakshina Kannada",
            new String[]{"Paddy - Jaya", "Arecanut - Sumangala", "Rubber - RRII 105", "Pepper - Karimunda"});
        d16.setSeedPrices(Map.of("Paddy", "₹112/kg", "Arecanut", "₹380/kg", "Rubber", "₹550/plant", "Pepper", "₹820/kg"));
        dealers.add(d16);
        
        Dealer d17 = new Dealer(17L, "Bantwal Seeds & Nursery", "Bantwal", "DAKSHINA KANNADA",
            "9448776655", "Bantwal, Dakshina Kannada",
            new String[]{"Coconut - Tall", "Cashew - BPP-8", "Banana - Robusta", "Pineapple - Mauritius"});
        d17.setSeedPrices(Map.of("Coconut", "₹47/nut", "Cashew", "₹880/kg", "Banana", "₹92/sucker", "Pineapple", "₹25/sucker"));
        dealers.add(d17);
        
        // UDUPI dealers
        Dealer d18 = new Dealer(18L, "Manipal Krishi Seva", "Manipal", "UDUPI",
            "9900334455", "Manipal, Udupi",
            new String[]{"Rice - Kavya", "Arecanut - Mohitnagar", "Coconut - ECT", "Betel Vine - Bangla"});
        d18.setSeedPrices(Map.of("Rice", "₹118/kg", "Arecanut", "₹390/kg", "Coconut", "₹52/nut", "Betel Vine", "₹8/vine"));
        dealers.add(d18);
        
        Dealer d19 = new Dealer(19L, "Kundapura Agri Store", "Kundapura", "UDUPI",
            "9845667788", "Kundapura, Udupi",
            new String[]{"Paddy - IR 64", "Coconut - D x T", "Cashew - VRI-3", "Pepper - Subhakara"});
        d19.setSeedPrices(Map.of("Paddy", "₹110/kg", "Coconut", "₹49/nut", "Cashew", "₹920/kg", "Pepper", "₹850/kg"));
        dealers.add(d19);
        
        // HASSAN dealers
        Dealer d20 = new Dealer(20L, "Hassan Seed Corporation", "Hassan", "HASSAN",
            "9731223344", "Hassan, Hassan District",
            new String[]{"Paddy - KMP 186", "Ragi - GPU 28", "Maize - NAH 2049", "Groundnut - ICGV 91114"});
        d20.setSeedPrices(Map.of("Paddy", "₹120/kg", "Ragi", "₹102/kg", "Maize", "₹215/kg", "Groundnut", "₹150/kg"));
        dealers.add(d20);
        
        Dealer d21 = new Dealer(21L, "Belur Agro Seeds", "Belur", "HASSAN",
            "9448998877", "Belur, Hassan District",
            new String[]{"Coffee - Arabica Selection", "Coconut - Chowghat Orange Dwarf", "Arecanut - Mangala", "Pepper - Panniyur-1"});
        d21.setSeedPrices(Map.of("Coffee", "₹1200/kg", "Coconut", "₹55/nut", "Arecanut", "₹360/kg", "Pepper", "₹790/kg"));
        dealers.add(d21);
        
        // CHIKMAGALUR dealers
        Dealer d22 = new Dealer(22L, "Malnad Coffee Seeds", "Chikmagalur", "CHIKMAGALUR",
            "9880556677", "Chikmagalur, Chikmagalur District",
            new String[]{"Coffee - Robusta S.795", "Coffee - Arabica Cauvery", "Pepper - Subhakara", "Cardamom - Njallani"});
        d22.setSeedPrices(Map.of("Coffee", "₹1350/kg", "Pepper", "₹850/kg", "Cardamom", "₹2500/kg"));
        dealers.add(d22);
        
        Dealer d23 = new Dealer(23L, "Mudigere Plantation Store", "Mudigere", "CHIKMAGALUR",
            "9449112233", "Mudigere, Chikmagalur District",
            new String[]{"Coffee - Arabica S.274", "Arecanut - Sumangala", "Banana - Poovan", "Coconut - West Coast Tall"});
        d23.setSeedPrices(Map.of("Coffee", "₹1280/kg", "Arecanut", "₹370/kg", "Banana", "₹88/sucker", "Coconut", "₹48/nut"));
        dealers.add(d23);
        
        // SHIMOGA dealers
        Dealer d24 = new Dealer(24L, "Shimoga Seed Centre", "Shimoga", "SHIMOGA",
            "9632778899", "Shimoga, Shimoga District",
            new String[]{"Paddy - BPT 5204", "Ragi - MR 6", "Maize - DHM 117", "Groundnut - TMV 2"});
        d24.setSeedPrices(Map.of("Paddy", "₹118/kg", "Ragi", "₹100/kg", "Maize", "₹210/kg", "Groundnut", "₹138/kg"));
        dealers.add(d24);
        
        Dealer d25 = new Dealer(25L, "Bhadravathi Agri Inputs", "Bhadravathi", "SHIMOGA",
            "9741445566", "Bhadravathi, Shimoga District",
            new String[]{"Arecanut - Mangala", "Coconut - Chowghat Green Dwarf", "Pepper - Panniyur-5", "Banana - Nendran"});
        d25.setSeedPrices(Map.of("Arecanut", "₹365/kg", "Coconut", "₹51/nut", "Pepper", "₹800/kg", "Banana", "₹90/sucker"));
        dealers.add(d25);
        
        // CHITRADURGA dealers
        Dealer d26 = new Dealer(26L, "Chitradurga Krishi Kendra", "Chitradurga", "CHITRADURGA",
            "9448334455", "Chitradurga, Chitradurga District",
            new String[]{"Groundnut - GPBD 4", "Ragi - GPU 67", "Maize - Arjun", "Cotton - Bt Bunny"});
        d26.setSeedPrices(Map.of("Groundnut", "₹142/kg", "Ragi", "₹96/kg", "Maize", "₹200/kg", "Cotton", "₹680/pkt"));
        dealers.add(d26);
        
        Dealer d27 = new Dealer(27L, "Hiriyur Seed House", "Hiriyur", "CHITRADURGA",
            "9900112233", "Hiriyur, Chitradurga District",
            new String[]{"Jowar - CSH 16", "Bajra - HHB 67", "Sunflower - KBSH 44", "Redgram - BRG 2"});
        d27.setSeedPrices(Map.of("Jowar", "₹125/kg", "Bajra", "₹110/kg", "Sunflower", "₹220/kg", "Redgram", "₹180/kg"));
        dealers.add(d27);
        
        // DAVANGERE dealers
        Dealer d28 = new Dealer(28L, "Davangere Agro Centre", "Davangere", "DAVANGERE",
            "9845998877", "Davangere, Davangere District",
            new String[]{"Cotton - Bt RCH 2", "Maize - 900M Gold", "Paddy - IET 21969", "Groundnut - TAG 24"});
        d28.setSeedPrices(Map.of("Cotton", "₹700/pkt", "Maize", "₹220/kg", "Paddy", "₹122/kg", "Groundnut", "₹148/kg"));
        dealers.add(d28);
        
        Dealer d29 = new Dealer(29L, "Harappanahalli Seeds", "Harappanahalli", "DAVANGERE",
            "9731556677", "Harappanahalli, Davangere District",
            new String[]{"Jowar - M 35-1", "Ragi - Indaf 9", "Sunflower - Morden", "Redgram - TS 3R"});
        d29.setSeedPrices(Map.of("Jowar", "₹118/kg", "Ragi", "₹94/kg", "Sunflower", "₹210/kg", "Redgram", "₹175/kg"));
        dealers.add(d29);
        
        // BELLARY dealers
        Dealer d30 = new Dealer(30L, "Bellary Seed Corporation", "Bellary", "BELLARY",
            "9448667788", "Bellary, Bellary District",
            new String[]{"Cotton - Bt Bunny", "Jowar - DSV 6", "Groundnut - K 6", "Sunflower - KBSH 53"});
        d30.setSeedPrices(Map.of("Cotton", "₹720/pkt", "Jowar", "₹130/kg", "Groundnut", "₹155/kg", "Sunflower", "₹230/kg"));
        dealers.add(d30);
        
        // MANDYA dealers
        Dealer d31 = new Dealer(31L, "Mandya Krishi Seva", "Mandya", "MANDYA",
            "9886334455", "Mandya, Mandya District",
            new String[]{"Sugarcane - CO 86032", "Paddy - KMP 186", "Ragi - MR 1", "Maize - CP 808"});
        d31.setSeedPrices(Map.of("Sugarcane", "₹88/kg", "Paddy", "₹125/kg", "Ragi", "₹105/kg", "Maize", "₹218/kg"));
        dealers.add(d31);
        
        // RAICHUR dealers
        Dealer d32 = new Dealer(32L, "Raichur Agro Traders", "Raichur", "RAICHUR",
            "9632112233", "Raichur, Raichur District",
            new String[]{"Cotton - Bt RCH 650", "Jowar - CSH 25", "Groundnut - Dh 86", "Redgram - LRG 52"});
        d32.setSeedPrices(Map.of("Cotton", "₹750/pkt", "Jowar", "₹135/kg", "Groundnut", "₹160/kg", "Redgram", "₹185/kg"));
        dealers.add(d32);
        
        // BIDAR dealers
        Dealer d33 = new Dealer(33L, "Bidar Seed House", "Bidar", "BIDAR",
            "9449556677", "Bidar, Bidar District",
            new String[]{"Wheat - Lok 1", "Chickpea - Vijay", "Jowar - M 35-1", "Redgram - BDN 711"});
        d33.setSeedPrices(Map.of("Wheat", "₹68/kg", "Chickpea", "₹175/kg", "Jowar", "₹120/kg", "Redgram", "₹178/kg"));
        dealers.add(d33);
        
        // GULBARGA dealers
        Dealer d34 = new Dealer(34L, "Kalaburagi Krishi Kendra", "Kalaburagi", "GULBARGA",
            "9741223344", "Kalaburagi, Gulbarga District",
            new String[]{"Wheat - HD 2967", "Chickpea - JG 11", "Jowar - CSH 16", "Cotton - Bt Mallika"});
        d34.setSeedPrices(Map.of("Wheat", "₹70/kg", "Chickpea", "₹180/kg", "Jowar", "₹128/kg", "Cotton", "₹690/pkt"));
        dealers.add(d34);
        
        // YADGIR dealers
        Dealer d35 = new Dealer(35L, "Yadgir Agro Centre", "Yadgir", "YADGIR",
            "9880445566", "Yadgir, Yadgir District",
            new String[]{"Cotton - Bt RCH 2", "Redgram - TS 3R", "Groundnut - ICGV 91114", "Sunflower - KBSH 44"});
        d35.setSeedPrices(Map.of("Cotton", "₹710/pkt", "Redgram", "₹182/kg", "Groundnut", "₹152/kg", "Sunflower", "₹225/kg"));
        dealers.add(d35);
        
        // CHAMARAJANAGAR dealers
        Dealer d36 = new Dealer(36L, "Chamarajanagar Seeds", "Chamarajanagar", "CHAMARAJANAGAR",
            "9448778899", "Chamarajanagar, Chamarajanagar District",
            new String[]{"Ragi - GPU 28", "Paddy - Jyothi", "Maize - Kaveri 50", "Groundnut - TMV 2"});
        d36.setSeedPrices(Map.of("Ragi", "₹98/kg", "Paddy", "₹115/kg", "Maize", "₹208/kg", "Groundnut", "₹140/kg"));
        dealers.add(d36);
        
        // KODAGU dealers
        Dealer d37 = new Dealer(37L, "Madikeri Coffee Estates", "Madikeri", "KODAGU",
            "9632334455", "Madikeri, Kodagu District",
            new String[]{"Coffee - Arabica Selection 9", "Coffee - Robusta S.795", "Pepper - Panniyur-1", "Cardamom - Njallani Green Gold"});
        d37.setSeedPrices(Map.of("Coffee", "₹1400/kg", "Pepper", "₹870/kg", "Cardamom", "₹2800/kg"));
        dealers.add(d37);
        
        Dealer d38 = new Dealer(38L, "Virajpet Plantation Centre", "Virajpet", "KODAGU",
            "9845112233", "Virajpet, Kodagu District",
            new String[]{"Coffee - Arabica Cauvery", "Arecanut - Sumangala", "Pepper - Subhakara", "Orange - Coorg"});
        d38.setSeedPrices(Map.of("Coffee", "₹1320/kg", "Arecanut", "₹375/kg", "Pepper", "₹840/kg", "Orange", "₹450/plant"));
        dealers.add(d38);
        
        logger.info("Initialized {} dealers across districts", dealers.size());
    }
    
    public List<Dealer> findDealers(String district, String crop, String city, int page, int size) {
        logger.info("Finding dealers - district: {}, crop: {}, city: {}, page: {}, size: {}",
            district, crop, city, page, size);
        
        List<Dealer> filtered = dealers.stream()
            .filter(d -> district == null || district.isBlank() || 
                        d.getDistrict().equalsIgnoreCase(district.trim()))
            .filter(d -> city == null || city.isBlank() || 
                        d.getCity().toLowerCase().contains(city.toLowerCase().trim()))
            .filter(d -> crop == null || crop.isBlank() || 
                        Arrays.stream(d.getAvailableSeeds())
                              .anyMatch(s -> s.equalsIgnoreCase(crop.trim())))
            .collect(Collectors.toList());
        
        // Pagination
        int start = page * size;
        int end = Math.min(start + size, filtered.size());
        
        if (start >= filtered.size()) {
            return new ArrayList<>();
        }
        
        return filtered.subList(start, end);
    }
    
    public long countDealers(String district, String crop, String city) {
        return dealers.stream()
            .filter(d -> district == null || district.isBlank() || 
                        d.getDistrict().equalsIgnoreCase(district.trim()))
            .filter(d -> city == null || city.isBlank() || 
                        d.getCity().toLowerCase().contains(city.toLowerCase().trim()))
            .filter(d -> crop == null || crop.isBlank() || 
                        Arrays.stream(d.getAvailableSeeds())
                              .anyMatch(s -> s.equalsIgnoreCase(crop.trim())))
            .count();
    }
}
