package com.example.data.local

import com.example.data.model.*
import com.example.security.PasswordHasher

object InitialDataSeeder {

    suspend fun seedInitialData(database: AppDatabase) {
        val userDao = database.userDao()
        val subjectDao = database.subjectDao()
        val questionDao = database.questionDao()
        val moderationDao = database.moderationDao()
        val auditDao = database.auditDao()
        val advisorDao = database.advisorDao()
        val quizDao = database.quizDao()

        // 1. Seed Users (Admin, Moderator, Advisor, Member) with secure salted hashes
        val saltAdmin = PasswordHasher.generateSalt()
        val saltMod = PasswordHasher.generateSalt()
        val saltAdv = PasswordHasher.generateSalt()
        val saltMem = PasswordHasher.generateSalt()

        val users = listOf(
            UserEntity(
                id = "usr_admin",
                username = "admin",
                email = "admin@edubank.org",
                passwordHash = PasswordHasher.hashPassword("admin123", saltAdmin),
                salt = saltAdmin,
                role = UserRole.ADMIN,
                displayName = "Dr. Arthur Pendelton",
                status = UserStatus.ACTIVE
            ),
            UserEntity(
                id = "usr_moderator",
                username = "moderator",
                email = "moderator@edubank.org",
                passwordHash = PasswordHasher.hashPassword("mod123", saltMod),
                salt = saltMod,
                role = UserRole.MODERATOR,
                displayName = "Elena Rostova",
                status = UserStatus.ACTIVE
            ),
            UserEntity(
                id = "usr_advisor",
                username = "advisor",
                email = "advisor@edubank.org",
                passwordHash = PasswordHasher.hashPassword("adv123", saltAdv),
                salt = saltAdv,
                role = UserRole.ADVISOR,
                displayName = "Prof. Marcus Vance",
                status = UserStatus.ACTIVE
            ),
            UserEntity(
                id = "usr_student",
                username = "student",
                email = "student@edubank.org",
                passwordHash = PasswordHasher.hashPassword("pass123", saltMem),
                salt = saltMem,
                role = UserRole.MEMBER,
                displayName = "Maya Lin",
                status = UserStatus.ACTIVE
            )
        )
        userDao.insertUsers(users)

        // 2. Seed Subjects
        val subjects = listOf(
            SubjectEntity(
                id = "subj_cs",
                name = "Computer Science",
                code = "CS-101",
                description = "Algorithms, Data Structures, Operating Systems & Distributed Architecture",
                iconName = "Computer",
                colorHex = "#2563EB",
                chapterCount = 4,
                questionCount = 8
            ),
            SubjectEntity(
                id = "subj_bio",
                name = "Medical & Life Sciences",
                code = "BIO-201",
                description = "Cellular Biology, Human Neurobiology, Genetics & Pathology",
                iconName = "Biotech",
                colorHex = "#059669",
                chapterCount = 3,
                questionCount = 6
            ),
            SubjectEntity(
                id = "subj_phy",
                name = "Physics & Engineering",
                code = "PHY-301",
                description = "Classical Mechanics, Thermodynamics, Electromagnetism & Circuits",
                iconName = "Bolt",
                colorHex = "#D97706",
                chapterCount = 3,
                questionCount = 5
            ),
            SubjectEntity(
                id = "subj_hist",
                name = "World History & Economics",
                code = "HIST-401",
                description = "Macroeconomic Policy, Global Trade, Governance & Historical Epochs",
                iconName = "Public",
                colorHex = "#7C3AED",
                chapterCount = 2,
                questionCount = 4
            )
        )
        subjectDao.insertSubjects(subjects)

        // 3. Seed Chapters & Topics
        val chapters = listOf(
            ChapterEntity("ch_dsa", "subj_cs", "Data Structures & Algorithms", 1, "Trees, graphs, dynamic programming"),
            ChapterEntity("ch_os", "subj_cs", "Operating Systems", 2, "Concurrency, virtual memory, process scheduling"),
            ChapterEntity("ch_db", "subj_cs", "Database Systems", 3, "ACID properties, indexing, relational algebra"),
            ChapterEntity("ch_neuro", "subj_bio", "Neurobiology & Synaptic Transmission", 1, "Action potentials, neurotransmitters"),
            ChapterEntity("ch_genetics", "subj_bio", "Molecular Genetics & DNA", 2, "Replication, transcription, translation"),
            ChapterEntity("ch_circuits", "subj_phy", "Digital Electronics & Circuits", 1, "Logic gates, Kirchhoff laws"),
            ChapterEntity("ch_thermo", "subj_phy", "Thermodynamics", 2, "Entropy, heat engines, Carnot cycle"),
            ChapterEntity("ch_macro", "subj_hist", "Global Macroeconomics", 1, "Fiscal policy, monetary systems, inflation")
        )
        subjectDao.insertChapters(chapters)

        val topics = listOf(
            TopicEntity("top_bst", "ch_dsa", "Binary Search Trees", "Tree traversals and AVL rotations"),
            TopicEntity("top_sort", "ch_dsa", "Sorting Algorithms", "Time & space complexity"),
            TopicEntity("top_mem", "ch_os", "Virtual Memory & Paging", "Page fault handling, TLB"),
            TopicEntity("top_acid", "ch_db", "Transactions & ACID", "Atomicity, Consistency, Isolation, Durability"),
            TopicEntity("top_synapse", "ch_neuro", "Chemical Synapses", "Vesicle fusion, receptors"),
            TopicEntity("top_dna", "ch_genetics", "DNA Replication", "Helicase, DNA Polymerase"),
            TopicEntity("top_logic", "ch_circuits", "Boolean Logic & Gates", "NAND, XOR, truth tables"),
            TopicEntity("top_entropy", "ch_thermo", "Second Law of Thermodynamics", "Entropy change"),
            TopicEntity("top_inflation", "ch_macro", "Monetary Policy & Inflation", "Central banking, interest rates")
        )
        subjectDao.insertTopics(topics)

        // 4. Seed Questions with full Options
        val q1 = QuestionEntity(
            id = "q_101",
            subjectId = "subj_cs",
            chapterId = "ch_dsa",
            topicId = "top_sort",
            title = "Average Time Complexity of QuickSort",
            content = "What is the expected average-case time complexity of randomized QuickSort when sorting an array of n distinct elements?",
            type = QuestionType.SINGLE_CHOICE,
            difficulty = DifficultyLevel.EASY,
            status = QuestionStatus.APPROVED,
            explanation = "QuickSort with randomized pivot selection partitions the array into roughly equal halves on average, yielding a recurrence of T(n) = 2T(n/2) + O(n), which solves to O(n log n).",
            tagsJson = "Algorithms, Sorting, Big-O, Divide and Conquer",
            authorUserId = "usr_admin",
            authorName = "Dr. Arthur Pendelton"
        )
        val opts1 = listOf(
            QuestionOptionEntity("opt_101_1", "q_101", "O(n)", false, "Linear time is achieved only in non-comparison sorts like Radix sort.", 0),
            QuestionOptionEntity("opt_101_2", "q_101", "O(n log n)", true, "Correct: Expected recursion tree depth is log n with linear work per level.", 1),
            QuestionOptionEntity("opt_101_3", "q_101", "O(n²)", false, "This is worst-case time complexity, occurring with deterministic pivots on sorted arrays.", 2),
            QuestionOptionEntity("opt_101_4", "q_101", "O(log n)", false, "Logarithmic time is impossible for comparison-based sorting.", 3)
        )

        val q2 = QuestionEntity(
            id = "q_102",
            subjectId = "subj_cs",
            chapterId = "ch_db",
            topicId = "top_acid",
            title = "ACID Properties in Relational Databases",
            content = "Which of the following guarantees belong to the four foundational ACID properties in transaction processing? (Select all that apply)",
            type = QuestionType.MULTI_CHOICE,
            difficulty = DifficultyLevel.MEDIUM,
            status = QuestionStatus.APPROVED,
            explanation = "ACID stands for Atomicity (all-or-nothing), Consistency (integrity preservation), Isolation (concurrent execution serializability), and Durability (committed data persists across crashes).",
            tagsJson = "Databases, SQL, ACID, Concurrency",
            authorUserId = "usr_admin",
            authorName = "Dr. Arthur Pendelton"
        )
        val opts2 = listOf(
            QuestionOptionEntity("opt_102_1", "q_102", "Atomicity", true, "Guarantees transactions are treated as single indivisible units.", 0),
            QuestionOptionEntity("opt_102_2", "q_102", "Availability", false, "Availability belongs to the CAP theorem, not ACID.", 1),
            QuestionOptionEntity("opt_102_3", "q_102", "Isolation", true, "Ensures concurrent transactions don't produce inconsistent states.", 2),
            QuestionOptionEntity("opt_102_4", "q_102", "Durability", true, "Ensures committed updates survive system failures.", 3)
        )

        val q3 = QuestionEntity(
            id = "q_103",
            subjectId = "subj_cs",
            chapterId = "ch_os",
            topicId = "top_mem",
            title = "HTTP/2 Multiplexing Protocol",
            content = "HTTP/2 introduces binary framing that allows multiple bidirectional streams to be interleaved concurrently over a single underlying TCP connection.",
            type = QuestionType.TRUE_FALSE,
            difficulty = DifficultyLevel.EASY,
            status = QuestionStatus.APPROVED,
            explanation = "True: HTTP/2 resolves head-of-line blocking at the application layer by multiplexing independent binary frames over one persistent TCP socket.",
            tagsJson = "Networking, HTTP2, Protocols, Web",
            authorUserId = "usr_advisor",
            authorName = "Prof. Marcus Vance"
        )
        val opts3 = listOf(
            QuestionOptionEntity("opt_103_1", "q_103", "True", true, "HTTP/2 streams multiplex concurrently without creating multiple TCP handshakes.", 0),
            QuestionOptionEntity("opt_103_2", "q_103", "False", false, "HTTP/1.1 required multiple connections; HTTP/2 multiplexes over one.", 1)
        )

        val q4 = QuestionEntity(
            id = "q_104",
            subjectId = "subj_cs",
            chapterId = "ch_dsa",
            topicId = "top_bst",
            title = "Binary Search Tree Traversal Diagram",
            content = "Examine the Binary Search Tree illustrated in the diagram. What is the correct In-Order Traversal sequence of the keys?",
            type = QuestionType.IMAGE_BASED,
            difficulty = DifficultyLevel.MEDIUM,
            status = QuestionStatus.APPROVED,
            explanation = "In-Order traversal follows Left Subtree -> Root -> Right Subtree. For any valid BST, an in-order traversal always produces the keys in strictly ascending sorted order.",
            diagramType = "BINARY_TREE",
            tagsJson = "Trees, BST, Diagrams, InOrder",
            authorUserId = "usr_admin",
            authorName = "Dr. Arthur Pendelton"
        )
        val opts4 = listOf(
            QuestionOptionEntity("opt_104_1", "q_104", "12, 25, 37, 50, 65, 75, 88", true, "In-Order on a BST always produces strictly sorted ascending values.", 0),
            QuestionOptionEntity("opt_104_2", "q_104", "50, 25, 12, 37, 75, 65, 88", false, "This represents Pre-Order traversal (Root, Left, Right).", 1),
            QuestionOptionEntity("opt_104_3", "q_104", "12, 37, 25, 65, 88, 75, 50", false, "This represents Post-Order traversal (Left, Right, Root).", 2),
            QuestionOptionEntity("opt_104_4", "q_104", "50, 25, 75, 12, 37, 65, 88", false, "This represents Breadth-First Level-Order traversal.", 3)
        )

        val q5 = QuestionEntity(
            id = "q_105",
            subjectId = "subj_cs",
            chapterId = "ch_os",
            topicId = "top_mem",
            title = "System Outage & Thrashing Scenario",
            content = "A cloud-based real-time analytics microservice running on Linux suddenly experiences severe degradation. CPU utilization spikes to 99%, but process throughput drops near zero. Diagnostic monitoring reveals an astronomical page fault rate (over 45,000 faults/second) and continuous disk I/O swap activity.",
            type = QuestionType.CASE_BASED,
            difficulty = DifficultyLevel.HARD,
            status = QuestionStatus.APPROVED,
            caseScenario = "System Telemetry Case:\nNode Instance: e2-standard-4 (16 GB RAM)\nVirtual Memory: 15.8 GB committed, Swap 8.0 GB (98% full)\nDiagnostic: kswapd0 is consuming 90% CPU. Worker threads are permanently blocked in D-state (disk wait) attempting to resolve memory references.",
            explanation = "This is a classic 'Thrashing' phenomenon in operating systems. When the aggregate working set of all active processes exceeds physical memory, the OS spends virtually all time swapping pages between disk and RAM rather than executing user instructions.",
            tagsJson = "Operating Systems, Case Study, Thrashing, Virtual Memory",
            authorUserId = "usr_moderator",
            authorName = "Elena Rostova"
        )
        val opts5 = listOf(
            QuestionOptionEntity("opt_105_1", "q_105", "Thrashing due to working set exceeding physical RAM", true, "Correct: Swapping overhead completely starves user-mode instruction execution.", 0),
            QuestionOptionEntity("opt_105_2", "q_105", "Deadlock caused by circular wait in worker threads", false, "Deadlock locks CPU threads into wait, not continuous 99% kswapd disk paging.", 1),
            QuestionOptionEntity("opt_105_3", "q_105", "DNS resolution saturation leading to TCP retransmits", false, "Page faults and swap metrics point directly to virtual memory management.", 2),
            QuestionOptionEntity("opt_105_4", "q_105", "Kernel buffer overflow in socket receive queue", false, "The high page fault rate specifically isolates memory paging overload.", 3)
        )

        val q6 = QuestionEntity(
            id = "q_106",
            subjectId = "subj_bio",
            chapterId = "ch_neuro",
            topicId = "top_synapse",
            title = "Chemical Synapse Mechanism",
            content = "Refer to the Synaptic Transmission diagram. Which ion influx through voltage-gated channels is directly required to trigger the exocytosis of neurotransmitter vesicles into the synaptic cleft?",
            type = QuestionType.IMAGE_BASED,
            difficulty = DifficultyLevel.MEDIUM,
            status = QuestionStatus.APPROVED,
            explanation = "When an action potential arrives at the axon terminal, voltage-gated Ca²⁺ channels open. Influx of Calcium ions activates synaptotagmin and SNARE complexes, driving synaptic vesicle membrane fusion and exocytosis.",
            diagramType = "SYNAPSE",
            tagsJson = "Neuroscience, Synapse, Calcium, Cellular",
            authorUserId = "usr_advisor",
            authorName = "Prof. Marcus Vance"
        )
        val opts6 = listOf(
            QuestionOptionEntity("opt_106_1", "q_106", "Calcium (Ca²⁺)", true, "Calcium influx binds synaptotagmin to trigger immediate vesicular fusion.", 0),
            QuestionOptionEntity("opt_106_2", "q_106", "Sodium (Na⁺)", false, "Na⁺ mediates action potential depolarization, not vesicle exocytosis.", 1),
            QuestionOptionEntity("opt_106_3", "q_106", "Potassium (K⁺)", false, "K⁺ efflux mediates membrane repolarization.", 2),
            QuestionOptionEntity("opt_106_4", "q_106", "Chloride (Cl⁻)", false, "Cl⁻ influx produces inhibitory postsynaptic potentials (IPSPs).", 3)
        )

        val q7 = QuestionEntity(
            id = "q_107",
            subjectId = "subj_phy",
            chapterId = "ch_circuits",
            topicId = "top_logic",
            title = "Digital Logic Circuit Output",
            content = "Examine the logic gate arrangement in the circuit diagram. If input A is 1 and input B is 0, what is the binary output Y of this circuit?",
            type = QuestionType.IMAGE_BASED,
            difficulty = DifficultyLevel.EASY,
            status = QuestionStatus.APPROVED,
            explanation = "The diagram displays an XOR gate feeding into an AND gate with an active HIGH enable. An XOR with inputs 1 and 0 produces 1. When 1 is ANDed with 1, output Y = 1.",
            diagramType = "LOGIC_GATE",
            tagsJson = "Physics, Digital Logic, Logic Gates, Circuits",
            authorUserId = "usr_admin",
            authorName = "Dr. Arthur Pendelton"
        )
        val opts7 = listOf(
            QuestionOptionEntity("opt_107_1", "q_107", "Output Y = 1 (HIGH)", true, "1 XOR 0 yields 1; 1 AND 1 yields 1.", 0),
            QuestionOptionEntity("opt_107_2", "q_107", "Output Y = 0 (LOW)", false, "Incorrect; the XOR evaluation of disparate inputs evaluates to TRUE.", 1),
            QuestionOptionEntity("opt_107_3", "q_107", "High Impedance (Floating)", false, "Digital CMOS push-pull gates drive definite voltage rails.", 2)
        )

        // Question 8: Pending review to demonstrate moderator dashboard!
        val q8 = QuestionEntity(
            id = "q_108",
            subjectId = "subj_hist",
            chapterId = "ch_macro",
            topicId = "top_inflation",
            title = "Central Bank Quantitative Tightening",
            content = "When a central bank initiates Quantitative Tightening (QT), it sells government bonds or lets them mature without reinvestment, which contracts the monetary base and elevates benchmark interest rates.",
            type = QuestionType.TRUE_FALSE,
            difficulty = DifficultyLevel.MEDIUM,
            status = QuestionStatus.PENDING_REVIEW,
            explanation = "True: Quantitative Tightening drains reserves from commercial banks, reducing liquidity and pushing yields upward.",
            tagsJson = "Economics, Central Banks, Macro, Monetary Policy",
            authorUserId = "usr_student",
            authorName = "Maya Lin"
        )
        val opts8 = listOf(
            QuestionOptionEntity("opt_108_1", "q_108", "True", true, "QT contracts the central bank balance sheet and absorbs market liquidity.", 0),
            QuestionOptionEntity("opt_108_2", "q_108", "False", false, "Quantitative Easing expands the balance sheet; QT contracts it.", 1)
        )

        val allQuestions = listOf(q1, q2, q3, q4, q5, q6, q7, q8)
        questionDao.insertQuestions(allQuestions)
        questionDao.insertOptions(opts1 + opts2 + opts3 + opts4 + opts5 + opts6 + opts7 + opts8)

        // 5. Seed Moderation Tasks
        moderationDao.insertTask(
            ModerationTaskEntity(
                id = "mod_1",
                questionId = "q_108",
                questionTitle = "Central Bank Quantitative Tightening",
                submittedByUserId = "usr_student",
                submittedByName = "Maya Lin",
                action = ModerationAction.SUBMITTED,
                feedbackNotes = "Submitted by student for community practice approval. Needs review on explanation clarity."
            )
        )

        // 6. Seed Sample Quiz Attempt for Maya Lin
        val sampleAttempt = QuizAttemptEntity(
            id = "att_001",
            userId = "usr_student",
            title = "CS & Systems Diagnostic Test",
            subjectId = "subj_cs",
            subjectName = "Computer Science",
            mode = QuizMode.PRACTICE,
            totalQuestions = 4,
            correctCount = 3,
            timeSpentSeconds = 142,
            scorePercentage = 75.0f,
            completedAt = System.currentTimeMillis() - 86400000L
        )
        quizDao.insertAttempt(sampleAttempt)

        val sampleAnswers = listOf(
            QuizAnswerEntity("ans_1", "att_001", "q_101", "opt_101_2", true, 28),
            QuizAnswerEntity("ans_2", "att_001", "q_102", "opt_102_1,opt_102_3,opt_102_4", true, 45),
            QuizAnswerEntity("ans_3", "att_001", "q_103", "opt_103_1", true, 19),
            QuizAnswerEntity("ans_4", "att_001", "q_104", "opt_104_2", false, 50) // Missed traversal
        )
        quizDao.insertAnswers(sampleAnswers)

        // 7. Seed Advisor Recommendation
        advisorDao.insertRecommendation(
            AdvisorRecommendationEntity(
                id = "rec_001",
                advisorUserId = "usr_advisor",
                advisorName = "Prof. Marcus Vance",
                targetUserId = "usr_student",
                targetUserName = "Maya Lin",
                subjectName = "Computer Science",
                topicName = "Binary Search Trees",
                title = "Focus on In-Order & Post-Order Traversal",
                note = "Your recent diagnostic test showed hesitation on Tree Traversals. Practice 5 dedicated BST questions in Practice Mode.",
                priority = "HIGH"
            )
        )

        // 8. Seed Audit Log Entries
        auditDao.insertLogs(listOf(
            AuditLogEntity("aud_1", "usr_admin", "ADMIN", "SYSTEM_SEED", "SYSTEM", "ALL", "Initial database seeded with 4 subjects and default user roles"),
            AuditLogEntity("aud_2", "usr_admin", "ADMIN", "ROLE_ASSIGNMENT", "USER", "usr_moderator", "Assigned MODERATOR privileges to Elena Rostova"),
            AuditLogEntity("aud_3", "usr_student", "MEMBER", "QUESTION_SUBMIT", "QUESTION", "q_108", "Submitted question for moderation review")
        ))
    }
}
