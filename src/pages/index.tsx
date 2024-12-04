import Mainheading from "@/components/shared/typography/Mainheading";
import {Box} from "@mui/material";
import {useSession} from "next-auth/react";

export default function Home() {
    const {data: session} = useSession();
    return (
        <>
            <Box flex={1}>
                <Mainheading>Welcome, {session?.user?.name}!</Mainheading>
            </Box>
        </>
    );
}
