package com.lifelink.ai.service.impl;

import com.lifelink.ai.config.AiProperties;
import com.lifelink.ai.dto.AiChatRequest;
import com.lifelink.ai.dto.AiChatResult;
import com.lifelink.ai.service.AiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MockAiChatServiceImpl implements AiChatService {

    private final AiProperties properties;

    @Override
    public AiChatResult chat(AiChatRequest request) {
        if (!Boolean.TRUE.equals(request.getResponseFormatJson())) {
            String content = mockChatContent(request);
            return new AiChatResult(content, properties.getProvider(), properties.getModel(), true, content);
        }
        if (isPsychologyTeacher(request)) {
            String content = mockCounselorJson(isEnglish(request), latestQuestionText(request));
            return new AiChatResult(content, properties.getProvider(), properties.getModel(), true, content);
        }
        String content = mockPhilosopherJson(resolveCode(request), isEnglish(request));
        return new AiChatResult(content, properties.getProvider(), properties.getModel(), true, content);
    }

    private boolean isEnglish(AiChatRequest request) {
        String systemPrompt = request == null ? "" : String.valueOf(request.getSystemPrompt());
        String userPrompt = request == null ? "" : String.valueOf(request.getUserPrompt());
        String messages = request == null ? "" : String.valueOf(request.getMessages());
        return systemPrompt.contains("en-US") || userPrompt.contains("en-US") || messages.contains("en-US");
    }

    private String mockChatContent(AiChatRequest request) {
        boolean english = isEnglish(request);
        String content = latestUserContent(request);
        String code = resolveCode(request);
        if (english) {
            if ("PSYCHOLOGY_TEACHER".equals(code)) {
                return mockCounselorChat(english, content);
            }
            if ("SOCRATES".equals(code)) {
                return "Let us not hurry past the word you used: \"" + content + "\". Do you mean a fact, a feeling, or a judgment about what a good life should be? In a Socratic spirit, I would first examine the term before offering a conclusion.";
            }
            if ("PLATO".equals(code)) {
                return "I would ask whether the situation you describe is only a shadow of a deeper disorder in the soul. In a Platonic style, the practical question is: what form of the good are you trying to serve, and which appetite is pulling you away from it?";
            }
            if ("ARISTOTLE".equals(code)) {
                return "I would distinguish the causes here: what happened, what habit produced your reaction, and what end you are aiming at. A more Aristotelian answer would look for the mean: not passivity, not excess, but a practiced response guided by practical wisdom.";
            }
            if ("KANT".equals(code)) {
                return "I would first ask what maxim you are about to act on. Could you will it as a rule for everyone in a similar situation? And does it treat the other person as an end, not merely as an instrument for your relief or advantage?";
            }
            if ("NIETZSCHE".equals(code)) {
                return "From a Nietzschean style, I would ask whether this question hides a fear of standing alone. Perhaps the issue is not to remove discomfort, but to see whether it can become material for creating your own values.";
            }
            if ("SCHOPENHAUER".equals(code)) {
                return "I would look at the desire underneath the pain. Often the suffering is not only that we lack something, but that the will keeps fastening itself to new objects. A little distance, restraint, and aesthetic quiet may help you loosen its grip.";
            }
            if ("CONFUCIUS".equals(code)) {
                return "I would return this to conduct and relationship. What would be the humane and properly measured response here? Begin with self-cultivation: make your words, role, and intention consistent before asking others to change.";
            }
            if ("ZHUANGZI".equals(code)) {
                return "In a Zhuangzi-like style, this question may be a knot made by holding one viewpoint too tightly. Try shifting the angle: what looks like a cage from one side may become an open field from another.";
            }
            return "Following this thinker’s style, I would connect your latest question with what we have already discussed, then ask you to clarify the value behind it before rushing to a final answer.";
        }
        if ("PSYCHOLOGY_TEACHER".equals(code)) {
            return mockCounselorChat(english, content);
        }
        if ("SOCRATES".equals(code)) {
            return "如果以苏格拉底式的方式来看，我们不妨先慢下来问：你说的“" + content + "”究竟指什么？它是一个事实、一种感受，还是你对生活应当如何的判断？也许答案要从澄清这个核心词开始。";
        }
        if ("PLATO".equals(code)) {
            return "以柏拉图式的方式看，你眼前的困扰也许只是影子，背后还有灵魂秩序的问题。我们可以问：你真正追求的是一时满足，还是更稳定、更接近善的生活状态？";
        }
        if ("ARISTOTLE".equals(code)) {
            return "按亚里士多德的思路，先区分几件事：发生了什么、它由哪些习惯造成、你的目的是什么。好的选择不是简单折中，而是在具体处境中用实践智慧找到合宜的中道。";
        }
        if ("KANT".equals(code)) {
            return "从康德式的角度看，关键不只是你想怎样做，而是你准备按什么准则行动。这个准则能否普遍化？它是否仍把对方当作目的，而不只是满足你需要的工具？";
        }
        if ("NIETZSCHE".equals(code)) {
            return "从尼采式的思想风格出发，我会追问：这个问题背后是否藏着一种害怕独自承担生命的恐惧？也许重点不是立刻消除不适，而是看它能否成为你重新创造自身价值的材料。";
        }
        if ("SCHOPENHAUER".equals(code)) {
            return "以叔本华的眼光看，痛苦常常不是因为某个对象本身，而是意志不断抓取、期待、落空。你可以先减少追逐，拉开一点距离，让审美、安静或同情给自己一个喘息处。";
        }
        if ("ZHUANGZI".equals(code)) {
            return "以庄子的思路看，这个问题像是因为抓住一个角度太紧而打成的结。你不妨换一个方向看：从一边看像牢笼的东西，从另一边看也许只是风经过的空处。";
        }
        if ("CONFUCIUS".equals(code)) {
            return "从孔子的思想风格看，这个问题可以回到修身与关系中来理解。先看自己在这件事中的言行是否合宜，再看怎样既不亏待他人，也不放弃成为更好的自己。";
        }
        return "按照这位思想家的风格，我会把你的最新问题和前面的对话连起来看：先不要急着要结论，而要看清你真正关心的价值是什么。";
    }

    private String latestUserContent(AiChatRequest request) {
        if (request == null || request.getMessages() == null || request.getMessages().isEmpty()) {
            return "这个问题";
        }
        for (int i = request.getMessages().size() - 1; i >= 0; i--) {
            if ("user".equals(request.getMessages().get(i).getRole())) {
                String content = request.getMessages().get(i).getContent();
                if (content == null || content.isBlank()) {
                    return "这个问题";
                }
                return content.length() > 30 ? content.substring(0, 30) : content;
            }
        }
        return "这个问题";
    }

    private String resolveCode(AiChatRequest request) {
        String text = request == null ? "" : String.valueOf(request.getMessages()) + " " + request.getSystemPrompt() + " " + request.getUserPrompt();
        String[] codes = {"PSYCHOLOGY_TEACHER", "SOCRATES", "PLATO", "ARISTOTLE", "KANT", "NIETZSCHE", "SCHOPENHAUER", "CONFUCIUS", "ZHUANGZI"};
        for (String code : codes) {
            if (text.contains(code)) {
                return code;
            }
        }
        return "";
    }

    private boolean isPsychologyTeacher(AiChatRequest request) {
        return "PSYCHOLOGY_TEACHER".equals(resolveCode(request))
                || String.valueOf(request == null ? "" : request.getSystemPrompt()).contains("心理老师")
                || String.valueOf(request == null ? "" : request.getUserPrompt()).contains("COUNSELOR_CARD");
    }

    private String latestQuestionText(AiChatRequest request) {
        String text = latestUserContent(request);
        if (!"这个问题".equals(text)) {
            return text;
        }
        String userPrompt = request == null ? "" : String.valueOf(request.getUserPrompt());
        int index = userPrompt.indexOf("userQuestion:");
        if (index >= 0) {
            String question = userPrompt.substring(index + "userQuestion:".length()).trim();
            int lineBreak = question.indexOf('\n');
            return lineBreak >= 0 ? question.substring(0, lineBreak).trim() : question;
        }
        return userPrompt;
    }

    private String mockCounselorJson(boolean english, String question) {
        String scene = counselorScene(question);
        if ("relationship".equals(scene)) {
            return english
                    ? counselorJson(
                    "I can sense that the relationship part is weighing on you. What feels difficult may not be only what the other person did, but also the uncertainty about your own needs, boundaries and how to express them without making things worse.",
                    "I would suggest not rushing into a final decision. First separate three things: what actually happened, what you guessed or feared, and what you need from the relationship. Then choose one calm sentence to express your boundary, such as: \"When this happens, I feel..., and I hope we can...\" This keeps the conversation concrete instead of turning it into blame.",
                    "Write two columns now: facts I know, and guesses I am making. Then write one sentence you can say to the other person without attacking them.",
                    "You do not need to decide everything today. Start by seeing your needs clearly.")
                    : counselorJson(
                    "我能感觉到这段关系让你有些拉扯。困难可能不只是对方做了什么，也包括你不确定自己的需要、边界，以及怎样表达才不会把局面弄得更僵。",
                    "我建议你先不要急着做最终决定。可以先分清三件事：客观发生了什么、我猜测或担心了什么、我在这段关系里真正需要什么。然后准备一句平和的表达，比如“当这件事发生时，我感到……，我希望我们可以……”。这样会比指责更容易让对话落到具体事情上。",
                    "现在写两栏：我确定的事实、我脑中的猜测。再写一句不攻击对方、但能表达自己边界的话。",
                    "你不需要今天就决定全部关系，先把自己的需要看清楚。");
        }
        if ("study".equals(scene)) {
            return english
                    ? counselorJson(
                    "I hear that the pressure may be making everything feel crowded at once. It is easy to mistake a pile of tasks for proof that you are not capable, but pressure and ability are not the same thing.",
                    "I would suggest shrinking the problem first. Pick only one task that matters most in the next 24 hours, then break it into a 25-minute step. Do not use today to judge your whole future. Use today to rebuild a bit of rhythm: one priority, one short block, one pause.",
                    "Write down: the most urgent task, the smallest next action, and when I will stop. Then do only the first 25 minutes.",
                    "You do not need perfect motivation first. Start with one small, clear step.")
                    : counselorJson(
                    "我能理解你现在可能觉得任务全都挤在一起，压力一大，人很容易把“事情很多”误解成“我不行”。但压力和能力不是一回事。",
                    "我会建议你先把问题缩小。只选未来 24 小时最重要的一件事，再把它拆成一个 25 分钟能开始的动作。今天先不要用来评判整个人生或能力，只用来恢复一点节奏：一个优先级、一个短时段、一次休息。",
                    "现在写下三行：最急的任务是什么；下一步最小动作是什么；我几点停下来休息。然后只做第一个 25 分钟。",
                    "你不需要先有完美状态，先做一个清楚的小动作就够了。");
        }
        if ("self_doubt".equals(scene)) {
            return english
                    ? counselorJson(
                    "I can hear the self-doubt in this. When people compare themselves for a long time, the mind often starts collecting only the evidence that says \"I am not enough\" and ignoring evidence on the other side.",
                    "I would not simply tell you to be confident. A more useful step is to check the evidence. Write down what triggered the comparison, what standard you are using, and whether that standard is realistic for your current stage. Then list one thing you have handled before, even if it was small.",
                    "Make a three-line record: What did I compare myself with? What evidence says I am failing? What evidence shows I am still trying or improving?",
                    "Self-doubt is loud, but it is not the whole truth about you.")
                    : counselorJson(
                    "我听得出这里有自我怀疑。人长期和别人比较时，大脑很容易只收集“我不够好”的证据，却忽略另一边的证据。",
                    "我不会简单跟你说“自信一点”。更有用的是先核对证据：这次比较是被什么触发的？你拿什么标准要求自己？这个标准对现在的你是否现实？然后再写下一件你曾经处理过的小事，哪怕很小，也是在提醒自己：我不是一无是处。",
                    "做一个三行记录：我在和谁或什么标准比较？哪些证据让我觉得失败？哪些证据说明我仍在努力或进步？",
                    "自我怀疑声音很大，但它不是关于你的全部事实。");
        }
        if ("anxiety".equals(scene)) {
            return english
                    ? counselorJson(
                    "I can sense that your mind may be running ahead into many possible outcomes. Anxiety often mixes facts, guesses and body tension together, making everything feel urgent at once.",
                    "I would suggest coming back to the present before making decisions. First name what is happening in your body. Then separate what you know from what you are predicting. Finally choose one controllable action within the next ten minutes, even if it is small.",
                    "Try this now: breathe out slowly three times, then write two lines: \"The fact is...\" and \"The worry says...\" Choose one small action after that.",
                    "You do not have to solve every possible future right now. Come back to this minute first.")
                    : counselorJson(
                    "我能感觉到你的大脑可能已经跑到很多可能的结果里了。焦虑常常会把事实、猜测和身体紧绷混在一起，让所有事情都像必须立刻解决。",
                    "我建议你先回到当下，再做判断。先说出身体正在发生什么，比如胸口紧、心跳快、脑子停不下来；再区分“我确定知道的事实”和“我正在预测的结果”；最后只选十分钟内能做的一件可控小事。",
                    "现在可以试试：慢慢呼气三次，然后写两行：“事实是……”和“担心在说……”。写完后只选一个小动作。",
                    "你不需要现在解决所有未来，先回到这一分钟。");
        }
        if (english) {
            return counselorJson(
                    "I can sense that this is not only about the situation itself, but also about the uncertainty it creates in you. Part of you may want to handle it rationally, while another part feels emotionally stuck. That inner conflict can be exhausting.",
                    "I would suggest not rushing into a final judgment. Try separating this into three parts: what actually happened, what you are feeling, and what you truly need. Often, we feel trapped not because there is no way forward, but because facts, fears and emotions are mixed together.",
                    "Write down three sentences: 1. The fact is... 2. I feel... 3. One small step I can take today is... Then choose only one small action to do today.",
                    "You do not need to solve everything at once. Start by gently bringing yourself out of the confusion.");
        }
        return counselorJson(
                "我能感觉到你现在的困扰并不只是这件事本身，而是它让你对自己、对关系或者对未来产生了很多不确定。你可能一边想理性处理，一边又很难把情绪放下，这种拉扯会让人特别累。",
                "我会建议你先不要急着做最终判断。可以先把这件事拆成三部分：第一，客观发生了什么；第二，我因此产生了什么感受；第三，我真正需要什么。很多时候，我们痛苦不是因为完全没有办法，而是事实、想象和情绪混在一起，让大脑以为自己被困住了。",
                "你可以现在写三句话：1. 事实是……；2. 我感到……；3. 我希望自己下一步先做到……。写完后，只选择一个今天能完成的小动作。",
                "你不需要一下子解决全部问题，先把自己从混乱里轻轻扶出来。");
    }

    private String mockCounselorChat(boolean english, String question) {
        String scene = counselorScene(question);
        if (english) {
            if ("relationship".equals(scene)) {
                return "I first understand why this relationship feels hard to hold right now. I would not rush you into deciding to leave or stay. We can first separate facts, feelings, and needs. A useful sentence may be: \"When this happened, I felt..., and I need us to talk about...\" Keep it specific and avoid turning the whole relationship into one argument.";
            }
            if ("study".equals(scene)) {
                return "I hear the pressure. I would suggest not treating the whole workload as one giant problem. Pick one task for the next 24 hours, break it into a 25-minute action, and decide when to stop. Your goal today is not to prove your worth. It is to restore a little rhythm.";
            }
            if ("self_doubt".equals(scene)) {
                return "I first want to slow down the self-judgment. Instead of forcing yourself to feel confident, check the evidence: what triggered the comparison, what standard are you using, and is it fair for your current stage? Self-doubt is information, but it is not a final verdict.";
            }
            if ("anxiety".equals(scene)) {
                return "I can hear that your mind is jumping ahead. Let us come back to the present first: name one body sensation, one fact you know, and one worry that is only a prediction. Then choose one action you can do in the next ten minutes.";
            }
            return "I hear that this matters to you. I would first help you slow it down: what happened, what you felt, what you thought it meant, and what you need now. You do not need to solve everything at once. Choose one small step you can control today.";
        }
        if ("relationship".equals(scene)) {
            return "我先理解你为什么会在这段关系里觉得难受。我不会直接替你决定分开或继续，我们可以先分清事实、感受和需要。你可以试着这样说：“当这件事发生时，我感到……，我希望我们能谈谈……”。先把话落到具体事情上，不要让一次沟通变成对整段关系的审判。";
        }
        if ("study".equals(scene)) {
            return "我能理解这种压力感。先不要把所有任务看成一个巨大的问题，今天只选未来 24 小时最重要的一件事，把它拆成一个 25 分钟能开始的动作，并提前设定几点停下来。今天的目标不是证明你多优秀，而是恢复一点节奏。";
        }
        if ("self_doubt".equals(scene)) {
            return "我想先帮你把自我评价放慢一点。与其逼自己立刻自信，不如先核对证据：这次比较是谁触发的？你用了什么标准？这个标准对现在的你公平吗？自我怀疑是一种信号，但它不是最后判决。";
        }
        if ("anxiety".equals(scene)) {
            return "我能听出你的脑子已经跑到很多可能结果里了。我们先回到当下：说出一个身体感觉、一个确定事实、一个只是预测的担心。然后选一个十分钟内能做的小动作，先让自己稳一点。";
        }
        return "我能听出来这件事对你有影响。我会先陪你把它放慢一点看：发生了什么、你当时有什么感受、你把它理解成了什么、你现在最需要什么。先不用急着解决全部问题，今天先选一个你能控制的小动作去做。";
    }

    private String counselorScene(String text) {
        String value = text == null ? "" : text.toLowerCase();
        if (containsAny(value, "恋爱", "分手", "对象", "男朋友", "女朋友", "关系", "喜欢", "暧昧", "伴侣", "love", "relationship", "break up", "partner", "boyfriend", "girlfriend")) {
            return "relationship";
        }
        if (containsAny(value, "学习", "考试", "作业", "论文", "工作", "上班", "老板", "任务", "压力", "study", "exam", "homework", "work", "job", "deadline")) {
            return "study";
        }
        if (containsAny(value, "自卑", "没用", "不如", "失败", "比较", "不配", "否定自己", "inferior", "useless", "failure", "compare", "not good enough")) {
            return "self_doubt";
        }
        if (containsAny(value, "焦虑", "紧张", "害怕", "恐慌", "睡不着", "心慌", "anxiety", "anxious", "panic", "nervous", "can't sleep")) {
            return "anxiety";
        }
        return "general";
    }

    private boolean containsAny(String text, String... patterns) {
        for (String pattern : patterns) {
            if (text.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    private String counselorJson(String understanding, String advice, String practice, String support) {
        return "{"
                + "\"understanding\":\"" + understanding + "\","
                + "\"advice\":\"" + advice + "\","
                + "\"practice\":\"" + practice + "\","
                + "\"support\":\"" + support + "\""
                + "}";
    }

    private String mockPhilosopherJson(String code, boolean english) {
        if (english) {
            if ("PLATO".equals(code)) {
                return json("The immediate situation may be only an appearance. A Platonic response would ask what kind of order in the soul would make this choice just, and what higher good the desire is imitating.",
                        "Are you pursuing the thing itself, or only its shadow?",
                        "This may sound too idealistic, because concrete pressures sometimes require action before the highest good is fully clear.",
                        "Look past the shadow and ask what good orders the soul.");
            }
            if ("ARISTOTLE".equals(code)) {
                return json("I would first classify the issue: emotion, habit, relationship, and purpose are not the same. A practical answer asks what end you seek and which stable habit would move you toward flourishing.",
                        "What would the balanced action be in this concrete situation?",
                        "A critic may say that balance can become indecision if it avoids necessary conflict or firm boundaries.",
                        "Good action is trained through habit and practical judgment.");
            }
            if ("KANT".equals(code)) {
                return json("The key is the maxim behind your action. If everyone acted by this rule, could the rule still be coherent? And would it respect every person involved as an end rather than a tool?",
                        "What rule would you be willing to make universal here?",
                        "This view can feel too strict if it underestimates emotion, vulnerability, or unequal power in the situation.",
                        "Act from a principle that preserves dignity.");
            }
            if ("NIETZSCHE".equals(code)) {
                return json("I would suspect the hidden obedience in the question. Is this your value, or a borrowed command wearing the mask of morality? The discomfort may be material for self-overcoming rather than a signal to shrink.",
                        "Whose value are you defending when you hesitate?",
                        "This sharpness may overlook the need for care, rest, and protection when someone is already exhausted.",
                        "Create values instead of merely inheriting fear.");
            }
            if ("SCHOPENHAUER".equals(code)) {
                return json("The pain may come from the will fastening itself to an object and promising peace if it is obtained. But satisfaction often gives birth to another demand, so distance and restraint matter.",
                        "Which desire is making itself appear absolutely necessary?",
                        "A critic may say this view is too pessimistic and underestimates constructive desire and human connection.",
                        "Loosen the will's grip before seeking peace.");
            }
            if ("CONFUCIUS".equals(code)) {
                return json("I would return the matter to self-cultivation and relationship. The question is not only what you want, but what response preserves humanity, proper measure, sincerity, and responsibility.",
                        "What would a humane and properly measured response look like?",
                        "This may be criticized if it seems to preserve harmony while neglecting individual boundaries or injustice.",
                        "Begin with self-cultivation, then set the relationship right.");
            }
            if ("ZHUANGZI".equals(code)) {
                return json("You may be caught in one fixed distinction, like a net called success, loss, or judgment. Shift the angle and the knot may loosen; the task is not to flee reality, but to stop worshiping one viewpoint.",
                        "What changes if you stop treating this one viewpoint as final?",
                        "A critic may say perspective-shifting is not enough when concrete duties or harms require direct action.",
                        "Loosen the fixed view and move more freely.");
            }
            return json("A Socratic answer would not rush to solve the question. It would first ask what the key word means, whether your belief is consistent, and what kind of life this belief serves.",
                    "What exactly do you mean by the central word in your question?",
                    "A critic may say that questioning alone can delay action when the situation already calls for a decision.",
                    "Clarify the concept before trusting the answer.");
        }
        if ("PLATO".equals(code)) {
            return json("眼前的处境也许只是表象。以柏拉图式的方式看，真正要问的是：这种欲望背后模仿着哪一种善？你的灵魂秩序是否让理性、激情和欲望各在其位？",
                    "你追求的是事物本身，还是它投下的影子？",
                    "这种看法可能显得过于理想化，因为现实压力有时要求人在善尚未完全清楚时先行动。",
                    "越过影子，去问什么善能安顿灵魂。");
        }
        if ("ARISTOTLE".equals(code)) {
            return json("我会先区分问题的类型：情绪、习惯、关系和目的并不是一回事。可实践的回答要问你追求什么目的，以及哪种稳定习惯能让你更接近好的生活。",
                    "在这个具体处境里，合宜的中道行动是什么？",
                    "反驳者会说，所谓平衡若回避必要冲突和清晰边界，就会变成犹豫。",
                    "好的行动来自习惯和实践智慧。");
        }
        if ("KANT".equals(code)) {
            return json("关键在于你行动背后的准则。如果每个人都按这个准则行动，它是否还能成立？它是否仍尊重每个相关者的人格，而不是把人当作缓解焦虑或获取利益的工具？",
                    "你愿意把此刻的行动准则变成普遍规则吗？",
                    "这种看法可能被批评为过于严整，因为它容易低估情感、脆弱和权力不对等。",
                    "按能维护人格尊严的原则行动。");
        }
        if ("NIETZSCHE".equals(code)) {
            return json("我会怀疑这个问题里隐藏的服从：这是你的价值，还是别人塞给你的命令披上了道德外衣？不适未必只是要被消除，它也可能成为你重新创造自己的材料。",
                    "你犹豫时，究竟在替谁的价值辩护？",
                    "这种锋利可能忽略人在疲惫和脆弱时对照顾、休息与保护的需要。",
                    "别只继承恐惧，去创造价值。");
        }
        if ("SCHOPENHAUER".equals(code)) {
            return json("痛苦也许来自意志把自己拴在某个对象上，并承诺得到它就会安宁。但满足之后常有新的欲望出现，所以更要学习距离、节制和片刻审美的解脱。",
                    "是哪一种欲望把自己伪装成了绝对必要？",
                    "反驳者会说，这种悲观低估了建设性欲望和人与人之间的真实支持。",
                    "先松开意志的抓握，再谈安宁。");
        }
        if ("CONFUCIUS".equals(code)) {
            return json("我会把问题带回修身与关系。重要的不只是你想要什么，而是怎样的回应能保全仁心、分寸、诚意和责任，使言行一致，也让关系回到合宜的位置。",
                    "怎样做才算既有仁心，又有分寸？",
                    "这种观点可能被批评为过分重视和谐，而忽略个人边界或不公正处境。",
                    "先修己，再以诚意安顿关系。");
        }
        if ("ZHUANGZI".equals(code)) {
            return json("你也许被某个固定判断困住了，像被一张名为成败、得失或评价的网罩住。换一个角度，结会松一点；重点不是逃避现实，而是不再膜拜唯一视角。",
                    "如果不把这个视角当成最终答案，事情会怎样变化？",
                    "反驳者会说，视角转换不足以处理真实伤害、责任或必须马上解决的问题。",
                    "松动固定的是非，人才有余地转身。");
        }
        return json("苏格拉底式的回答不会急着解决问题，而会先问核心词是什么意思、你的信念是否一致，以及这种信念服务于什么样的生活。",
                "你问题里的核心词，究竟是什么意思？",
                "反驳者会说，追问本身可能拖延行动，尤其当处境已经需要明确决定时。",
                "先澄清概念，再相信答案。");
    }

    private String json(String viewpoint, String questionBack, String objection, String summary) {
        return "{"
                + "\"viewpoint\":\"" + viewpoint + "\","
                + "\"questionBack\":\"" + questionBack + "\","
                + "\"objection\":\"" + objection + "\","
                + "\"summary\":\"" + summary + "\""
                + "}";
    }
}
