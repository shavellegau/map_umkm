const functions = require('firebase-functions');
const admin = require('firebase-admin');

// 1. Inisialisasi Firebase Admin SDK
// Ini penting agar kode server bisa mengakses database Anda.
admin.initializeApp();
const db = admin.firestore();

// Jumlah reward yang akan diberikan (misalnya 5000 Rupiah)
// Sesuaikan nilai ini sesuai kebijakan reward Anda
const REWARD_AMOUNT = 5000; 

/**
 * Cloud Function yang terpicu otomatis setiap kali dokumen baru dibuat di koleksi 'users'.
 */
exports.processReferralReward = functions.firestore
    .document('users/{newUserId}')
    .onCreate(async (snapshot, context) => {
        const newUserData = snapshot.data();
        const newUserId = context.params.newUserId; // UID pengguna baru (referee)
        const usedCode = newUserData.usedReferralCode;

        // Cek 1: Hanya proses jika ada kode yang digunakan dan statusnya 'pending'
        if (!usedCode || newUserData.referralRewardStatus !== 'pending') {
            console.log(`User ${newUserId} tidak menggunakan kode referral atau status bukan 'pending'.`);
            return null; 
        }

        // 2. Cari pemilik kode referral lama (referrer)
        const referrerQuery = await db.collection('users')
            .where('ownReferralCode', '==', usedCode)
            .limit(1)
            .get();

        if (referrerQuery.empty) {
            console.log(`Kode referral tidak ditemukan atau tidak valid: ${usedCode}.`);
            // Update status pengguna baru menjadi kode tidak valid
            return db.collection('users').doc(newUserId).update({ 
                referralRewardStatus: 'invalid_code' 
            });
        }

        const referrerDoc = referrerQuery.docs[0];
        const referrerId = referrerDoc.id; // UID pengguna lama (referrer)

        // 3. --- LOGIKA PEMBERIAN REWARD (Transaksi Aman) ---
        // Transaksi menjamin kedua update (referrer dan referee) sukses bersamaan
        return db.runTransaction(async (transaction) => {
            
            // Ambil data terbaru referrer (penting untuk menghindari data corruption)
            const referrerRef = db.collection('users').doc(referrerId);
            const currentReferrerDoc = await transaction.get(referrerRef);
            
            // Definisikan saldo yang ada (jika belum ada, dimulai dari 0)
            const referrerBalance = currentReferrerDoc.data().rewardBalance || 0;
            const refereeBalance = newUserData.rewardBalance || 0;

            // A. Update REWARD untuk PENGGUNA LAMA (Referrer)
            transaction.update(referrerRef, {
                rewardBalance: referrerBalance + REWARD_AMOUNT,
                // Optional: Bisa juga menambahkan 'referralCount' di sini
            });

            // B. Update REWARD untuk PENGGUNA BARU (Referee)
            transaction.update(db.collection('users').doc(newUserId), {
                rewardBalance: refereeBalance + REWARD_AMOUNT,
                referralRewardStatus: 'rewarded', // Status diubah menjadi 'rewarded'
            });

        }).then(() => {
            console.log(`REFERRAL SUKSES: User ${referrerId} & ${newUserId} mendapatkan reward sebesar ${REWARD_AMOUNT}.`);
            return null;
        }).catch((error) => {
            console.error("TRANSAKSI REFERRAL GAGAL:", error);
            // Anda bisa menambahkan logika notifikasi error di sini
            return null;
        });
    });