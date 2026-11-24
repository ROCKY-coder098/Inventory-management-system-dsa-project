import java.io.*;
import java.util.Scanner;

/* ---------- Item: one stock item ---------- */
class Item {
    private final int id;
    private final String name;
    private final String category;
    private int quantity;
    private final double price;

    Item(int id, String name, String category, int quantity, double price) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.price = price;
    }

    int getId() { return id; }
    String getName() { return name; }
    String getCategory() { return category; }
    int getQuantity() { return quantity; }
    double getPrice() { return price; }
    void setQuantity(int q) { this.quantity = q; }

    @Override
    public String toString() {
        return "ID " + id + " | " + name + " | " + category + " | Qty " + quantity
                + " | Rs. " + String.format("%.2f", price);
    }
}

/* ---------- HashTable: separate chaining, key = item ID ---------- */
class HashTable {
    private static class Node {
        Item item;
        Node next;
        Node(Item item) { this.item = item; }
    }

    private final Node[] buckets;
    private int count = 0;

    HashTable(int size) { buckets = new Node[size]; }

    int hash(int id) { return id % buckets.length; }

    int size() { return count; }

    void put(Item item) {                       // O(1) average
        int index = hash(item.getId());
        Node node = new Node(item);
        if (buckets[index] == null) {
            buckets[index] = node;
        } else {                                // collision: add at the end of the chain
            Node cur = buckets[index];
            while (cur.next != null) cur = cur.next;
            cur.next = node;
        }
        count++;
    }

    Item get(int id) {                          // O(1) average
        Node cur = buckets[hash(id)];
        while (cur != null) {
            if (cur.item.getId() == id) return cur.item;
            cur = cur.next;
        }
        return null;
    }

    Item remove(int id) {                       // O(1) average
        int index = hash(id);
        Node cur = buckets[index], prev = null;
        while (cur != null) {
            if (cur.item.getId() == id) {
                if (prev == null) buckets[index] = cur.next;
                else prev.next = cur.next;
                count--;
                return cur.item;
            }
            prev = cur;
            cur = cur.next;
        }
        return null;
    }

    Item[] toArray() {                          // O(N + buckets)
        Item[] arr = new Item[count];
        int k = 0;
        for (Node head : buckets) {
            for (Node cur = head; cur != null; cur = cur.next) arr[k++] = cur.item;
        }
        return arr;
    }
}

/* ---------- BST: key = item name (ignoring case) ---------- */
class BST {
    private static class BSTNode {
        Item item;
        BSTNode left, right;
        BSTNode(Item item) { this.item = item; }
    }

    private BSTNode root;

    void insert(Item item) { root = insert(root, item); }

    private BSTNode insert(BSTNode node, Item item) {
        if (node == null) return new BSTNode(item);
        if (item.getName().compareToIgnoreCase(node.item.getName()) < 0)
            node.left = insert(node.left, item);
        else
            node.right = insert(node.right, item);
        return node;
    }

    Item search(String name) {
        BSTNode cur = root;
        while (cur != null) {
            int c = name.compareToIgnoreCase(cur.item.getName());
            if (c == 0) return cur.item;
            cur = (c < 0) ? cur.left : cur.right;
        }
        return null;
    }

    void delete(String name) { root = delete(root, name); }

    private BSTNode delete(BSTNode node, String name) {
        if (node == null) return null;
        int c = name.compareToIgnoreCase(node.item.getName());
        if (c < 0) node.left = delete(node.left, name);
        else if (c > 0) node.right = delete(node.right, name);
        else {
            if (node.left == null) return node.right;       // leaf or only right child
            if (node.right == null) return node.left;       // only left child
            BSTNode succ = node.right;                      // two children: in-order successor
            while (succ.left != null) succ = succ.left;
            node.item = succ.item;
            node.right = delete(node.right, succ.item.getName());
        }
        return node;
    }

    void inorder(Visitor v) { inorder(root, v); }

    private void inorder(BSTNode node, Visitor v) {
        if (node == null) return;
        inorder(node.left, v);
        v.visit(node.item);
        inorder(node.right, v);
    }

    interface Visitor { void visit(Item item); }
}

/* ---------- Main: menu and all operations ---------- */
public class InventoryManagementSystem {
    private static final String FILE_NAME = "inventory.txt";
    private static final int TABLE_SIZE = 7;

    private static final HashTable table = new HashTable(TABLE_SIZE);
    private static final BST bst = new BST();
    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        loadFile();
        boolean running = true;
        while (running) {
            showMenu();
            String choice = readLine("");
            switch (choice) {
                case "1": addItem(); break;
                case "2": searchById(); break;
                case "3": searchByName(); break;
                case "4": updateStock(); break;
                case "5": deleteItem(); break;
                case "6": displayAll(); break;
                case "7": sortedReport(); break;
                case "8": lowStockCheck(); break;
                case "9": saveFile(); System.out.println("Inventory saved. Goodbye!"); running = false; break;
                default: System.out.println("Invalid choice. Enter 1 to 9.");
            }
        }
    }

    private static void showMenu() {
        System.out.println();
        System.out.println("===== INVENTORY MANAGEMENT SYSTEM =====");
        System.out.println("1. Add item");
        System.out.println("2. Search by ID");
        System.out.println("3. Search by name");
        System.out.println("4. Update stock");
        System.out.println("5. Delete item");
        System.out.println("6. Display all items (A-Z)");
        System.out.println("7. Sorted report");
        System.out.println("8. Low stock check");
        System.out.println("9. Exit");
        System.out.print("Enter your choice: ");
    }

    /* ----- input helpers (validation) ----- */
    private static String readLine(String prompt) {
        if (!prompt.isEmpty()) System.out.print(prompt);
        if (!sc.hasNextLine()) { saveFile(); System.exit(0); }   // end of input: save and stop
        return sc.nextLine().trim();
    }

    private static String readText(String prompt) {
        while (true) {
            String s = readLine(prompt).replace("|", " ").trim();
            if (!s.isEmpty()) return s;
            System.out.println("Invalid input. This field cannot be empty.");
        }
    }

    private static int readInt(String prompt, int min) {
        while (true) {
            String s = readLine(prompt);
            try {
                int v = Integer.parseInt(s);
                if (v >= min) return v;
            } catch (NumberFormatException e) { /* fall through */ }
            System.out.println("Invalid input. Enter a valid number.");
        }
    }

    private static double readPrice(String prompt) {
        while (true) {
            String s = readLine(prompt);
            try {
                double v = Double.parseDouble(s);
                if (v >= 0 && !Double.isNaN(v) && !Double.isInfinite(v)) return v;
            } catch (NumberFormatException e) { /* fall through */ }
            System.out.println("Invalid input. Enter a valid number.");
        }
    }

    /* ----- file handling ----- */
    private static void loadFile() {
        File f = new File(FILE_NAME);
        if (!f.exists()) return;                       // no file: start with no items
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split("\\|");
                if (p.length != 5) continue;           // skip a wrongly formatted line
                try {
                    int id = Integer.parseInt(p[0].trim());
                    String name = p[1].trim(), cat = p[2].trim();
                    int qty = Integer.parseInt(p[3].trim());
                    double price = Double.parseDouble(p[4].trim());
                    if (id <= 0 || qty < 0 || price < 0 || name.isEmpty()) continue;
                    if (table.get(id) != null || bst.search(name) != null) continue; // duplicates
                    Item item = new Item(id, name, cat, qty, price);
                    table.put(item);
                    bst.insert(item);
                } catch (NumberFormatException e) { /* skip bad line */ }
            }
        } catch (IOException e) {
            System.out.println("Could not read " + FILE_NAME + ": " + e.getMessage());
        }
    }

    private static void saveFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_NAME))) {
            bst.inorder(item -> pw.println(item.getId() + "|" + item.getName() + "|"
                    + item.getCategory() + "|" + item.getQuantity() + "|" + item.getPrice()));
        } catch (IOException e) {
            System.out.println("Could not save " + FILE_NAME + ": " + e.getMessage());
        }
    }

    /* ----- menu operations ----- */
    private static void addItem() {
        int id = readInt("Enter item ID: ", 1);
        if (table.get(id) != null) {
            System.out.println("Item ID already exists. Item not added.");
            return;
        }
        String name = readText("Enter item name: ");
        if (bst.search(name) != null) {
            System.out.println("Item name already exists. Item not added.");
            return;
        }
        String category = readText("Enter category: ");
        int qty = readInt("Enter quantity: ", 0);
        double price = readPrice("Enter price: ");
        Item item = new Item(id, name, category, qty, price);
        table.put(item);
        bst.insert(item);
        System.out.println("Item added successfully. (stored in bucket " + table.hash(id) + ")");
    }

    private static void searchById() {
        int id = readInt("Enter item ID: ", 1);
        Item item = table.get(id);
        System.out.println(item == null ? "Item not found." : "Found: " + item);
    }

    private static void searchByName() {
        String name = readText("Enter item name: ");
        Item item = bst.search(name);
        System.out.println(item == null ? "Item not found." : "Found: " + item);
    }

    private static void updateStock() {
        int id = readInt("Enter item ID: ", 1);
        Item item = table.get(id);
        if (item == null) { System.out.println("Item not found."); return; }
        System.out.println("Current stock of " + item.getName() + ": " + item.getQuantity());
        int type = readChoice("1. Add stock  2. Remove stock\nEnter choice: ", 1, 2);
        int amount = readInt("Enter quantity: ", 1);
        if (type == 1) {
            item.setQuantity(item.getQuantity() + amount);
        } else {
            if (amount > item.getQuantity()) { System.out.println("Not enough stock. Stock not changed."); return; }
            item.setQuantity(item.getQuantity() - amount);
        }
        System.out.println("Stock updated. New quantity of " + item.getName() + ": " + item.getQuantity());
    }

    private static int readChoice(String prompt, int min, int max) {
        while (true) {
            int v = readInt(prompt, min);
            if (v <= max) return v;
            System.out.println("Invalid choice. Enter " + min + " to " + max + ".");
        }
    }

    private static void deleteItem() {
        int id = readInt("Enter item ID: ", 1);
        Item removed = table.remove(id);               // remove from Hash Table
        if (removed == null) { System.out.println("Item not found."); return; }
        bst.delete(removed.getName());                 // remove from BST
        System.out.println("Item deleted: " + removed.getName());
    }

    private static void displayAll() {
        if (table.size() == 0) { System.out.println("Inventory is empty."); return; }
        System.out.println();
        System.out.printf("%-15s %-6s %-14s %-6s %s%n", "Name", "ID", "Category", "Qty", "Price (Rs.)");
        bst.inorder(item -> System.out.printf("%-15s %-6d %-14s %-6d %.2f%n",
                item.getName(), item.getId(), item.getCategory(), item.getQuantity(), item.getPrice()));
    }

    private static void sortedReport() {
        if (table.size() == 0) { System.out.println("Inventory is empty."); return; }
        int key = readChoice("Sort by: 1. Quantity  2. Price\nEnter choice: ", 1, 2);
        Item[] arr = table.toArray();
        mergeSort(arr, key);
        System.out.println();
        System.out.printf("%-9s %-15s %-8s %s%n", "Position", "Name", "Qty", "Price (Rs.)");
        for (int i = 0; i < arr.length; i++)
            System.out.printf("%-9d %-15s %-8d %.2f%n", i, arr[i].getName(), arr[i].getQuantity(), arr[i].getPrice());
    }

    private static void lowStockCheck() {
        if (table.size() == 0) { System.out.println("Inventory is empty."); return; }
        int limit = readInt("Enter low stock limit: ", 0);
        Item[] arr = table.toArray();
        mergeSort(arr, 1);                             // sort by quantity
        int pos = firstAtLeast(arr, limit);            // binary search
        System.out.println("First position with quantity >= " + limit + " is " + pos + ".");
        if (pos == 0) {
            System.out.println("No low stock items.");
        } else {
            System.out.println("Low stock items (quantity below " + limit + "):");
            for (int i = 0; i < pos; i++)
                System.out.println("  " + arr[i].getName() + " - Qty " + arr[i].getQuantity());
        }
    }

    /* ----- Merge Sort (key 1 = quantity, key 2 = price), ascending, stable ----- */
    private static double keyOf(Item it, int key) {
        return key == 1 ? it.getQuantity() : it.getPrice();
    }

    private static void mergeSort(Item[] a, int key) {
        if (a.length < 2) return;
        mergeSort(a, new Item[a.length], 0, a.length - 1, key);
    }

    private static void mergeSort(Item[] a, Item[] tmp, int lo, int hi, int key) {
        if (lo >= hi) return;
        int mid = (lo + hi) / 2;
        mergeSort(a, tmp, lo, mid, key);
        mergeSort(a, tmp, mid + 1, hi, key);
        int i = lo, j = mid + 1, k = lo;
        while (i <= mid && j <= hi)
            tmp[k++] = (keyOf(a[i], key) <= keyOf(a[j], key)) ? a[i++] : a[j++];
        while (i <= mid) tmp[k++] = a[i++];
        while (j <= hi) tmp[k++] = a[j++];
        for (k = lo; k <= hi; k++) a[k] = tmp[k];
    }

    /* ----- Binary Search: first position whose quantity >= limit (array sorted by quantity) ----- */
    private static int firstAtLeast(Item[] a, int limit) {
        int lo = 0, hi = a.length;
        while (lo < hi) {
            int mid = (lo + hi) / 2;
            if (a[mid].getQuantity() >= limit) hi = mid;
            else lo = mid + 1;
        }
        return lo;
    }
}